package com.nexus.backend.admin;

import com.nexus.backend.entity.User;
import com.nexus.backend.support.CapturingMail;
import com.nexus.backend.support.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(CapturingMail.class)
@SpringBootTest(properties = {"app.moderation.email=moderacion@primercommit.test",
        "app.auth.require-verified-email=true", "app.test.context=admin-sin-limites"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminWithoutLimitsIntegrationTest {

    private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Europe/Madrid"));

    @Autowired
    private MockMvc mvc;
    @Autowired
    private TestUsers users;
    @Autowired
    private CapturingMail.Sender mail;

    @BeforeEach
    void clean() {
        mail.sent.clear();
    }

    @Test
    void anAdministratorCanShareAsManyEventsAsNeededWithoutAVerifiedEmailOrModerationAlerts() throws Exception {
        String admin = users.bearer(users.create("Persona", User.Role.ADMIN, false));
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
        String admin = users.bearer(users.create("Persona", User.Role.ADMIN, true));
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
        String admin = users.bearer(users.create("Persona", User.Role.ADMIN, true));
        for (int i = 0; i < 15; i++) {
            mvc.perform(post("/api/discussions").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                            .content(discussion(i)))
                    .andExpect(status().isCreated());
        }

        String ana = users.bearer(users.create("Persona", User.Role.USER, true));
        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/api/discussions").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                            .content(discussion(i)))
                    .andExpect(status().isCreated());
        }
        mvc.perform(post("/api/discussions").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                        .content(discussion(11)))
                .andExpect(status().isTooManyRequests());
        mvc.perform(post("/api/discussions").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content(discussion(99)))
                .andExpect(status().isCreated());
    }

    @Test
    void otherPeopleKeepTheirLimits() throws Exception {
        String bea = users.bearer(users.create("Persona", User.Role.USER, true));
        mvc.perform(post("/api/testimonials").header("Authorization", bea).contentType(MediaType.APPLICATION_JSON)
                        .content(testimonial()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isPending").value(true));
        mvc.perform(post("/api/testimonials").header("Authorization", bea).contentType(MediaType.APPLICATION_JSON)
                        .content(testimonial()))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/events").header("Authorization", users.bearer(users.create("Persona", User.Role.USER, false)))
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
}
