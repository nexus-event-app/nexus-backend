package com.nexus.backend.security.google;

import com.nexus.backend.i18n.Languages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * «Continuar con Google» por redirección (flujo de código de OAuth 2.0 con OpenID Connect): la web no carga ningún
 * script de Google. La web manda a la persona a {@code /api/oauth2/authorize/google?lang=es}; Google la devuelve a
 * {@code /api/oauth2/callback/google} y desde ahí se la redirige a la web («/registro/google» en su idioma) con un
 * código de un solo uso en el fragmento (#), que no llega a ningún servidor ni queda en los registros.
 *
 * <p>Solo existe si hay GOOGLE_CLIENT_ID: sin credenciales la aplicación arranca igual y la web oculta el botón. La
 * sesión HTTP se usa únicamente durante el ida y vuelta a Google (para el parámetro «state») y se cierra al volver.
 */
@Slf4j
@Configuration
@ConditionalOnExpression("!'${app.google.client-id:}'.isBlank()")
public class GoogleOAuthConfig {

    static final String AUTHORIZE_BASE = "/api/oauth2/authorize";
    static final String CALLBACK_BASE = "/api/oauth2/callback";
    static final String RETURN_PATH = "/registro/google";
    private static final String LANGUAGE_ATTRIBUTE = GoogleOAuthConfig.class.getName() + ".language";
    private static final Set<String> LANGUAGES = Set.of("es", "ca", "en");

    @Value("${app.public-url:http://localhost:5173}")
    private String publicUrl;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${app.google.client-id}") String clientId,
            @Value("${app.google.client-secret:}") String clientSecret) {
        // La vuelta de Google entra por la dirección pública de la web (que reenvía /api al backend), no por la del
        // backend: tiene que coincidir con la registrada en Google Cloud.
        ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(baseUrl() + CALLBACK_BASE + "/{registrationId}")
                .scope("openid", "email", "profile")
                .build();
        return new InMemoryClientRegistrationRepository(google);
    }

    @Bean
    @Order(0)
    public SecurityFilterChain googleLoginFilterChain(HttpSecurity http, ClientRegistrationRepository registrations,
                                                      GoogleAccountService accounts) throws Exception {
        http
                .securityMatcher(PathPatternRequestMatcher.withDefaults().matcher("/api/oauth2/**"))
                // Solo hay peticiones GET de ida y vuelta; el parámetro «state» de OAuth protege la vuelta.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // La autenticación de Google no se guarda en la sesión: la sesión de la web es siempre el JWT.
                .securityContext(context -> context.securityContextRepository(new RequestAttributeSecurityContextRepository()))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(login -> login
                        .authorizationEndpoint(endpoint -> endpoint
                                .baseUri(AUTHORIZE_BASE)
                                .authorizationRequestResolver(rememberingLanguage(registrations)))
                        .redirectionEndpoint(endpoint -> endpoint.baseUri(CALLBACK_BASE + "/*"))
                        .successHandler((request, response, authentication) -> {
                            OidcUser user = (OidcUser) authentication.getPrincipal();
                            GoogleProfile profile = new GoogleProfile(user.getSubject(), user.getEmail(),
                                    Boolean.TRUE.equals(user.getEmailVerified()), user.getFullName());
                            String fragment = switch (accounts.handleLogin(profile)) {
                                case GoogleLoginOutcome.SignedIn signedIn -> "code=" + signedIn.code();
                                case GoogleLoginOutcome.SignupPending pending -> "signup=" + pending.token();
                                case GoogleLoginOutcome.Rejected rejected -> "error=" + rejected.reason();
                            };
                            redirectToWeb(request, response, fragment);
                        })
                        .failureHandler((request, response, exception) ->
                                redirectToWeb(request, response, "error=" + failureReason(exception))));
        return http.build();
    }

    /** Guarda el idioma de la web al salir hacia Google, para volver a la página en ese idioma. */
    private static OAuth2AuthorizationRequestResolver rememberingLanguage(ClientRegistrationRepository registrations) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(registrations, AUTHORIZE_BASE);
        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                return remember(request, resolver.resolve(request));
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                return remember(request, resolver.resolve(request, clientRegistrationId));
            }

            private OAuth2AuthorizationRequest remember(HttpServletRequest request, OAuth2AuthorizationRequest result) {
                String language = request.getParameter("lang");
                if (result != null && language != null && LANGUAGES.contains(language)) {
                    request.getSession().setAttribute(LANGUAGE_ATTRIBUTE, language);
                }
                return result;
            }
        };
    }

    private void redirectToWeb(HttpServletRequest request, HttpServletResponse response, String fragment)
            throws IOException {
        Locale locale = Languages.DEFAULT;
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object language = session.getAttribute(LANGUAGE_ATTRIBUTE);
            if (language instanceof String tag) {
                locale = Locale.forLanguageTag(tag);
            }
            // El ida y vuelta ha terminado: la sesión ya no sirve para nada.
            session.invalidate();
        }
        response.sendRedirect(baseUrl() + Languages.localizePath(locale, RETURN_PATH) + "#" + fragment);
    }

    /** «cancelled» si la persona pulsó «Cancelar» en Google; «failed» para cualquier otro problema. */
    static String failureReason(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth
                && "access_denied".equals(oauth.getError().getErrorCode())) {
            return "cancelled";
        }
        log.warn("No se ha podido entrar con Google: {}", exception.getMessage());
        return "failed";
    }

    private String baseUrl() {
        return publicUrl.replaceAll("/+$", "");
    }
}
