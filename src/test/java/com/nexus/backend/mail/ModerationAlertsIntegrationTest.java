package com.nexus.backend.mail;

import com.nexus.backend.entity.User;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.JwtUtil;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Al llegar un testimonio, se avisa por email al correo de moderación con el texto y el enlace al panel. */
@SpringBootTest(properties = {"app.moderation.email=moderacion@primercommit.test", "app.public-url=https://primercommit.test"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ModerationAlertsIntegrationTest {

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
    private CapturingEmailSender mail;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void clearMail() {
        mail.sent.clear();
    }

    @Test
    void aNewTestimonialSendsTheFullTextToTheModerationEmail() throws Exception {
        User author = user();
        mvc.perform(post("/api/testimonials").header("Authorization", bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Marta","role":"Desarrolladora junior","location":"Valencia",
                                 "content":"Encontré mi primer trabajo gracias a la comunidad.\\nY aprendí <muchísimo>."}
                                """))
                .andExpect(status().isCreated());

        EmailMessage alert = awaitEmail();
        assertThat(alert.to()).isEqualTo("moderacion@primercommit.test");
        assertThat(alert.subject()).isEqualTo("Nuevo testimonio de Marta para revisar");
        assertThat(alert.text())
                .contains("Marta (Desarrolladora junior · Valencia)")
                .contains("Encontré mi primer trabajo gracias a la comunidad.")
                .contains("https://primercommit.test/admin/testimonios");
        // El texto de quien escribe no puede meter HTML en el email; los saltos de línea se respetan.
        assertThat(alert.html())
                .contains("Y aprend&iacute; &lt;much&iacute;simo&gt;.")
                .doesNotContain("<much")
                .contains("comunidad.<br>Y aprend")
                .contains("href=\"https://primercommit.test/admin/testimonios\"");
        assertThat(mail.sent).hasSize(1);
    }

    private EmailMessage awaitEmail() throws InterruptedException {
        for (int i = 0; i < 50 && mail.sent.isEmpty(); i++) {
            Thread.sleep(100);
        }
        assertThat(mail.sent).as("aviso de moderación enviado").isNotEmpty();
        return mail.sent.get(0);
    }

    private User user() {
        User user = new User();
        user.setName("Marta");
        user.setEmail("moderation-" + UUID.randomUUID() + "@test.com");
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String bearer(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
    }
}
