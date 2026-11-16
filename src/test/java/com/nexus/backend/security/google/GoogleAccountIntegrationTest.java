package com.nexus.backend.security.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.User;
import com.nexus.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Entrar con Google sin pasar por Google: lo que ocurre con los datos que devuelve y con los códigos de un solo uso. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoogleAccountIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private GoogleAccountService googleAccounts;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void someoneNewOnlyGetsAnAccountAfterAcceptingTheTerms() throws Exception {
        String email = uniqueEmail();
        GoogleProfile profile = new GoogleProfile(uniqueSubject(), email.toUpperCase(), true, "  Lucía   Prueba ");

        GoogleLoginOutcome outcome = googleAccounts.handleLogin(profile);

        assertThat(outcome).isInstanceOf(GoogleLoginOutcome.SignupPending.class);
        assertThat(userRepository.findByEmail(email)).isEmpty();
        String token = ((GoogleLoginOutcome.SignupPending) outcome).token();

        postJson("/api/auth/google/pending", Map.of("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lucía Prueba"))
                .andExpect(jsonPath("$.email").value(email));

        // Sin marcar la casilla no se crea nada.
        postJson("/api/auth/google/complete", Map.of("token", token, "acceptTerms", false))
                .andExpect(status().isBadRequest());
        assertThat(userRepository.findByEmail(email)).isEmpty();

        postJson("/api/auth/google/complete", Map.of("token", token, "acceptTerms", true))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.emailVerified").value(true));

        User created = userRepository.findByEmail(email).orElseThrow();
        assertThat(created.getGoogleSubject()).isEqualTo(profile.subject());
        assertThat(created.getPassword()).isNull();
        assertThat(created.getTermsAcceptedAt()).isNotNull();
        assertThat(created.isEmailVerified()).isTrue();

        // El código de alta es de un solo uso.
        postJson("/api/auth/google/complete", Map.of("token", token, "acceptTerms", true))
                .andExpect(status().isUnauthorized());
        // Una cuenta sin contraseña no se abre con email y contraseña.
        postJson("/api/auth/login", Map.of("email", email, "password", "cualquier-cosa"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anExistingAccountWithTheSameEmailIsLinkedAndSignedIn() throws Exception {
        User existing = saveUser(uniqueEmail(), true);
        String subject = uniqueSubject();

        GoogleLoginOutcome outcome =
                googleAccounts.handleLogin(new GoogleProfile(subject, existing.getEmail(), true, "Otro nombre"));

        assertThat(outcome).isInstanceOf(GoogleLoginOutcome.SignedIn.class);
        User linked = userRepository.findById(existing.getId()).orElseThrow();
        assertThat(linked.getGoogleSubject()).isEqualTo(subject);
        assertThat(linked.isEmailVerified()).isTrue();
        assertThat(linked.getName()).isEqualTo(existing.getName());

        String code = ((GoogleLoginOutcome.SignedIn) outcome).code();
        postJson("/api/auth/google/exchange", Map.of("token", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.id").value(existing.getId()));
        // El código se canjea una sola vez.
        postJson("/api/auth/google/exchange", Map.of("token", code)).andExpect(status().isUnauthorized());

        // La próxima vez se la reconoce por la cuenta de Google aunque cambie el email de Google.
        GoogleLoginOutcome again = googleAccounts.handleLogin(new GoogleProfile(subject, uniqueEmail(), true, "X"));
        assertThat(again).isInstanceOf(GoogleLoginOutcome.SignedIn.class);
    }

    @Test
    void withoutAVerifiedEmailNothingIsLinkedNorCreated() {
        User existing = saveUser(uniqueEmail(), true);

        GoogleLoginOutcome outcome =
                googleAccounts.handleLogin(new GoogleProfile(uniqueSubject(), existing.getEmail(), false, "Intruso"));

        assertThat(outcome).isEqualTo(new GoogleLoginOutcome.Rejected(GoogleAccountService.REJECTED_UNVERIFIED));
        assertThat(userRepository.findById(existing.getId()).orElseThrow().getGoogleSubject()).isNull();
    }

    @Test
    void aDisabledAccountCannotSignInWithGoogle() {
        User disabled = saveUser(uniqueEmail(), false);

        GoogleLoginOutcome outcome =
                googleAccounts.handleLogin(new GoogleProfile(uniqueSubject(), disabled.getEmail(), true, "Nombre"));

        assertThat(outcome).isEqualTo(new GoogleLoginOutcome.Rejected(GoogleAccountService.REJECTED_DISABLED));
    }

    @Test
    void unknownCodesAreRejectedAndTheButtonStaysHiddenWithoutCredentials() throws Exception {
        postJson("/api/auth/google/exchange", Map.of("token", "codigo-inventado")).andExpect(status().isUnauthorized());
        postJson("/api/auth/google/pending", Map.of("token", "codigo-inventado")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.google").value(false));
    }

    @Test
    void theNameFallsBackToTheEmailWhenGoogleGivesNone() {
        assertThat(GoogleAccountService.nameFor(new GoogleProfile("s", "maria.lopez@example.com", true, null)))
                .isEqualTo("maria.lopez");
        assertThat(GoogleAccountService.nameFor(new GoogleProfile("s", "a@example.com", true, "Ana López")))
                .isEqualTo("Ana López");
    }

    private ResultActions postJson(String path, Object body) throws Exception {
        return mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private User saveUser(String email, boolean enabled) {
        User user = new User();
        user.setEmail(email);
        user.setName("Persona de prueba");
        user.setPassword(passwordEncoder.encode("contrasena-segura"));
        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    private static String uniqueEmail() {
        return "g-" + UUID.randomUUID() + "@example.com";
    }

    private static String uniqueSubject() {
        return "google-" + UUID.randomUUID();
    }
}
