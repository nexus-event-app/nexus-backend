package com.nexus.backend.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.UserToken;
import com.nexus.backend.enums.UserTokenType;
import com.nexus.backend.mail.EmailMessage;
import com.nexus.backend.mail.EmailSender;
import com.nexus.backend.repository.UserTokenRepository;
import com.nexus.backend.security.OneTimeTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verificación de email y recuperación de contraseña de extremo a extremo, leyendo los enlaces de los emails. */
@SpringBootTest(properties = "app.auth.require-verified-email=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountFlowIntegrationTest {

    private static final Pattern TOKEN = Pattern.compile("token=([A-Za-z0-9_-]{43})");

    @TestConfiguration
    static class CapturingMail {
        @Bean
        @Primary
        CapturingEmailSender capturingEmailSender() {
            return new CapturingEmailSender();
        }
    }

    static class CapturingEmailSender implements EmailSender {
        final List<EmailMessage> sent = new CopyOnWriteArrayList<>();

        @Override
        public void send(EmailMessage message) {
            sent.add(message);
        }
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CapturingEmailSender mail;
    @Autowired
    private UserTokenRepository tokenRepository;

    @BeforeEach
    void clearMail() {
        mail.sent.clear();
    }

    @Test
    void newUsersMustVerifyTheirEmailBeforePosting() throws Exception {
        String email = uniqueEmail();
        String jwt = register(email);
        EmailMessage verification = awaitEmailTo(email);
        assertThat(verification.subject()).contains("Confirma tu email");
        assertThat(verification.html()).contains("/verificar-email?token=");

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + jwt))
                .andExpect(jsonPath("$.emailVerified").value(false));
        postDiscussion(jwt).andExpect(status().isForbidden()).andExpect(jsonPath("$.error").value("EMAIL_NOT_VERIFIED"));

        String token = tokenFrom(verification);
        verify(token).andExpect(status().isNoContent());

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + jwt))
                .andExpect(jsonPath("$.emailVerified").value(true));
        postDiscussion(jwt).andExpect(status().isCreated());
        verify(token).andExpect(status().isBadRequest());
    }

    @Test
    void resendingTheVerificationInvalidatesThePreviousLink() throws Exception {
        String email = uniqueEmail();
        String jwt = register(email);
        String first = tokenFrom(awaitEmailTo(email));
        mail.sent.clear();

        mvc.perform(post("/api/auth/resend-verification").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isAccepted());
        String second = tokenFrom(awaitEmailTo(email));

        verify(first).andExpect(status().isBadRequest());
        verify(second).andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/resend-verification").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest());
    }

    @Test
    void expiredLinksAreRejected() throws Exception {
        String email = uniqueEmail();
        register(email);
        String token = tokenFrom(awaitEmailTo(email));
        UserToken stored = tokenRepository.findByHashAndType(OneTimeTokens.hash(token), UserTokenType.EMAIL_VERIFICATION)
                .orElseThrow();
        stored.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        tokenRepository.save(stored);

        verify(token).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("caducado")));
    }

    @Test
    void passwordResetDoesNotRevealAccountsAndClosesOldSessions() throws Exception {
        mvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nadie-" + UUID.randomUUID() + "@test.com\"}"))
                .andExpect(status().isAccepted());

        String email = uniqueEmail();
        String oldJwt = register(email);
        awaitEmailTo(email);
        mail.sent.clear();

        mvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email.toUpperCase() + "\"}"))
                .andExpect(status().isAccepted());
        EmailMessage reset = awaitEmailTo(email);
        assertThat(reset.subject()).contains("Restablece tu contraseña");
        assertThat(mail.sent).hasSize(1);
        String token = tokenFrom(reset);

        resetPassword(token, "corta").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        Thread.sleep(1100); // los JWT guardan la fecha de emisión en segundos
        resetPassword(token, "NuevaClave123").andExpect(status().isNoContent());
        resetPassword(token, "OtraClave123").andExpect(status().isBadRequest());

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + oldJwt)).andExpect(status().isUnauthorized());
        login(email, "Clave12345").andExpect(status().isUnauthorized());
        String newJwt = objectMapper.readTree(login(email, "NuevaClave123").andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).path("token").asText();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + newJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerified").value(true));
    }

    private String register(String email) throws Exception {
        String body = "{\"name\":\"Ana Prueba\",\"email\":\"" + email + "\",\"password\":\"Clave12345\",\"acceptTerms\":true}";
        String response = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("token").asText();
    }

    private ResultActions postDiscussion(String jwt) throws Exception {
        return mvc.perform(post("/api/discussions").header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Mi primera duda\",\"content\":\"Contenido suficientemente largo\",\"category\":\"GENERAL\"}"));
    }

    private ResultActions verify(String token) throws Exception {
        return mvc.perform(post("/api/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\"}"));
    }

    private ResultActions resetPassword(String token, String password) throws Exception {
        return mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\",\"password\":\"" + password + "\"}"));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    /** Los emails se envían en segundo plano: espera hasta 5 segundos a que llegue. */
    private EmailMessage awaitEmailTo(String email) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            for (EmailMessage message : mail.sent) {
                if (message.to().equals(email)) {
                    return message;
                }
            }
            Thread.sleep(100);
        }
        throw new AssertionError("No llegó ningún email a " + email);
    }

    private static String tokenFrom(EmailMessage message) {
        Matcher matcher = TOKEN.matcher(message.text());
        assertThat(matcher.find()).as("el email contiene un enlace con token").isTrue();
        return matcher.group(1);
    }

    private static String uniqueEmail() {
        return "u-" + UUID.randomUUID() + "@test.com";
    }
}
