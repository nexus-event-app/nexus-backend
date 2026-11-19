package com.nexus.backend.admin;

import com.nexus.backend.entity.User;
import com.nexus.backend.mail.EmailMessage;
import com.nexus.backend.mail.EmailSender;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La administración no tiene límites: ni de eventos al día, ni de testimonios, ni de publicaciones por hora en el
 * foro, ni necesita el email verificado. Lo suyo se publica sin pasar por moderación. El resto sí tiene límites.
 * Contexto propio ({@code app.test.context}) para que los contadores por IP no se mezclen con otras pruebas.
 */
@SpringBootTest(properties = {"app.moderation.email=moderacion@primercommit.test",
        "app.auth.require-verified-email=true", "app.test.context=admin-sin-limites"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminWithoutLimitsIntegrationTest {

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

    private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Europe/Madrid"));

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CapturingEmailSender mail;

    @BeforeEach
    void clean() {
        mail.sent.clear();
    }

    @Test
    void anAdministratorCanShareAsManyEventsAsNeededWithoutAVerifiedEmailOrModerationAlerts() throws Exception {
        String admin = bearer(user(User.Role.ADMIN, false));
        for (int i = 1; i <= 8; i++) {
            mvc.perform(post("/api/events").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                            .content(event(i)))
                    .andExpect(status().isCreated());
        }
        Thread.sleep(500);
        assertThat(mail.sent).as("sin avisos de moderación por lo que publica la administración").isEmpty();
    }

    @Test
    void anAdministratorCanPublishSeveralTestimonialsAndTheyGoLiveAtOnce() throws Exception {
        String admin = bearer(user(User.Role.ADMIN, true));
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/testimonials").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                            .content(testimonial()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.isActive").value(true))
                    .andExpect(jsonPath("$.isPending").value(false));
        }
        Thread.sleep(500);
        assertThat(mail.sent).isEmpty();
    }

    @Test
    void anAdministratorHasNoHourlyLimitInTheForumButOtherPeopleDo() throws Exception {
        String admin = bearer(user(User.Role.ADMIN, true));
        for (int i = 0; i < 15; i++) {
            mvc.perform(post("/api/discussions").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                            .content(discussion(i)))
                    .andExpect(status().isCreated());
        }

        String ana = bearer(user(User.Role.USER, true));
        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/api/discussions").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                            .content(discussion(i)))
                    .andExpect(status().isCreated());
        }
        mvc.perform(post("/api/discussions").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content(discussion(11)))
                .andExpect(status().isTooManyRequests());
        // Aunque otra persona haya agotado el límite desde la misma conexión, la administración sigue pudiendo.
        mvc.perform(post("/api/discussions").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content(discussion(99)))
                .andExpect(status().isCreated());
    }

    @Test
    void otherPeopleKeepTheirLimits() throws Exception {
        String bea = bearer(user(User.Role.USER, true));
        mvc.perform(post("/api/testimonials").header("Authorization", bea).contentType(MediaType.APPLICATION_JSON)
                        .content(testimonial()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isPending").value(true));
        mvc.perform(post("/api/testimonials").header("Authorization", bea).contentType(MediaType.APPLICATION_JSON)
                        .content(testimonial()))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/events").header("Authorization", bearer(user(User.Role.USER, false)))
                        .contentType(MediaType.APPLICATION_JSON).content(event(100)))
                .andExpect(status().isForbidden());
    }

    private static String event(int n) {
        return "{\"url\":\"https://www.meetup.com/admin/events/" + n + "/\",\"title\":\"Evento de la comunidad " + n
                + "\",\"description\":\"Charla para empezar en tecnología y conocer gente.\",\"date\":\""
                + TODAY.plusDays(3) + "\",\"mode\":\"ONLINE\",\"forWomen\":false}";
    }

    private static String testimonial() {
        return "{\"name\":\"Equipo\",\"role\":\"Administración\",\"content\":\"Gracias a la comunidad aprendemos "
                + "juntas y juntos cada día.\"}";
    }

    private static String discussion(int n) {
        return "{\"title\":\"Bienvenida a la comunidad " + n + "\",\"content\":\"Un hilo para presentarse y "
                + "conocernos.\",\"category\":\"AYUDA_TECNICA\"}";
    }

    private User user(User.Role role, boolean verified) {
        User user = new User();
        user.setName(role == User.Role.ADMIN ? "Admin" : "Persona");
        user.setEmail("limits-" + UUID.randomUUID() + "@test.com");
        user.setRole(role);
        user.setEnabled(true);
        user.setEmailVerifiedAt(verified ? LocalDateTime.now() : null);
        return userRepository.save(user);
    }

    private String bearer(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
    }
}
