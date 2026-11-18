package com.nexus.backend.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.CommunityEvent;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.EventMode;
import com.nexus.backend.mail.EmailMessage;
import com.nexus.backend.mail.EmailSender;
import com.nexus.backend.repository.CommunityEventRepository;
import com.nexus.backend.repository.ReportRepository;
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
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Eventos que comparte la comunidad: quién puede publicar, qué se ve, límites, borrado, reportes y aviso por email. */
@SpringBootTest(properties = {"app.moderation.email=moderacion@primercommit.test", "app.auth.require-verified-email=true"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventIntegrationTest {

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
    private ObjectMapper json;
    @Autowired
    private CommunityEventRepository eventRepository;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CapturingEmailSender mail;

    @BeforeEach
    void clean() {
        reportRepository.deleteAll();
        eventRepository.deleteAll();
        mail.sent.clear();
    }

    @Test
    void sharingAnEventPublishesItAtOnceWithoutTheAuthorAndEmailsModeration() throws Exception {
        User ana = user("Ana", User.Role.USER, true);

        create(ana, event("Meetup de React en Valencia", TODAY.plusDays(3), "PRESENCIAL", "Valencia", false))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.host").value("meetup.com"))
                .andExpect(jsonPath("$.mine").value(true));

        mvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Meetup de React en Valencia"))
                .andExpect(jsonPath("$.content[0].city").value("Valencia"))
                .andExpect(jsonPath("$.content[0].address").value("Calle Mayor, 1"))
                // Sin sesión no es «mío», y nunca se dice quién lo compartió.
                .andExpect(jsonPath("$.content[0].mine").value(false))
                .andExpect(jsonPath("$.content[0].author").doesNotExist())
                .andExpect(jsonPath("$.content[0].authorName").doesNotExist());

        EmailMessage alert = awaitEmail();
        assertThat(alert.to()).isEqualTo("moderacion@primercommit.test");
        assertThat(alert.subject()).isEqualTo("Nuevo evento en la comunidad: Meetup de React en Valencia");
        assertThat(alert.text()).contains("Valencia").contains("https://www.meetup.com/");
        assertThat(alert.html()).contains("/admin/eventos");
    }

    @Test
    void onlyUpcomingVisibleEventsAreListedSoonestFirstAndCanBeFiltered() throws Exception {
        User ana = user("Ana", User.Role.USER, true);
        saved(ana, "Ya pasó", TODAY.minusDays(1), EventMode.ONLINE, false, true);
        saved(ana, "Oculto por moderación", TODAY.plusDays(1), EventMode.ONLINE, false, false);
        saved(ana, "Dentro de una semana", TODAY.plusDays(7), EventMode.PRESENCIAL, true, true);
        saved(ana, "Hoy mismo", TODAY, EventMode.ONLINE, false, true);
        saved(ana, "Mañana, de mujeres", TODAY.plusDays(1), EventMode.ONLINE, true, true);

        mvc.perform(get("/api/events"))
                .andExpect(jsonPath("$.content[*].title", contains("Hoy mismo", "Mañana, de mujeres", "Dentro de una semana")));
        mvc.perform(get("/api/events").param("forWomen", "true"))
                .andExpect(jsonPath("$.content[*].title", contains("Mañana, de mujeres", "Dentro de una semana")));
        mvc.perform(get("/api/events").param("forWomen", "false"))
                .andExpect(jsonPath("$.content[*].title", contains("Hoy mismo")));
        mvc.perform(get("/api/events").param("mode", "PRESENCIAL"))
                .andExpect(jsonPath("$.content[*].title", contains("Dentro de una semana")));
    }

    @Test
    void filtersByCityWithoutCaringAboutCaseAndByDay() throws Exception {
        User ana = user("Ana", User.Role.USER, true);
        saved(ana, "En Madrid", TODAY.plusDays(2), EventMode.PRESENCIAL, false, true, "Madrid");
        saved(ana, "En Barcelona", TODAY.plusDays(2), EventMode.PRESENCIAL, false, true, "Barcelona");
        saved(ana, "En madrid, otro día", TODAY.plusDays(5), EventMode.PRESENCIAL, true, true, "madrid ");
        saved(ana, "Online", TODAY.plusDays(2), EventMode.ONLINE, false, true);

        mvc.perform(get("/api/events").param("city", "MADRID"))
                .andExpect(jsonPath("$.content[*].title", contains("En Madrid", "En madrid, otro día")));
        mvc.perform(get("/api/events").param("day", TODAY.plusDays(2).toString()))
                .andExpect(jsonPath("$.content", hasSize(3)));
        mvc.perform(get("/api/events").param("day", TODAY.plusDays(2).toString()).param("city", "barcelona"))
                .andExpect(jsonPath("$.content[*].title", contains("En Barcelona")));
        // Una entrada por ciudad aunque esté escrita distinto, en orden alfabético.
        mvc.perform(get("/api/events/cities")).andExpect(jsonPath("$", contains("Barcelona", "Madrid")));
    }

    @Test
    void theCalendarMarksTheDaysWithEventsAndWhetherAnyIsForWomen() throws Exception {
        User ana = user("Ana", User.Role.USER, true);
        saved(ana, "Pasado", TODAY.minusDays(1), EventMode.ONLINE, false, true);
        saved(ana, "Uno", TODAY.plusDays(1), EventMode.ONLINE, false, true);
        saved(ana, "Dos, de mujeres", TODAY.plusDays(1), EventMode.ONLINE, true, true);
        saved(ana, "Oculto", TODAY.plusDays(3), EventMode.ONLINE, false, false);
        saved(ana, "Tres", TODAY.plusDays(4), EventMode.ONLINE, false, true);

        mvc.perform(get("/api/events/calendar").param("from", TODAY.minusDays(7).toString())
                        .param("to", TODAY.plusDays(30).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].date").value(TODAY.plusDays(1).toString()))
                .andExpect(jsonPath("$[0].count").value(2))
                .andExpect(jsonPath("$[0].forWomen").value(true))
                .andExpect(jsonPath("$[1].date").value(TODAY.plusDays(4).toString()))
                .andExpect(jsonPath("$[1].forWomen").value(false));
        mvc.perform(get("/api/events/calendar").param("from", TODAY.toString())
                        .param("to", TODAY.plusDays(200).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anAdministratorCanFixAnEventFromThePanel() throws Exception {
        User ana = user("Ana", User.Role.USER, true);
        User admin = user("Admin", User.Role.ADMIN, true);
        CommunityEvent typo = saved(ana, "Meetup de Reakt", TODAY.plusDays(1), EventMode.PRESENCIAL, false, true,
                "Doofinder C. de Cronos, 63 · Madrid");
        Map<String, Object> fixed = event("Meetup de React", TODAY.plusDays(2), "PRESENCIAL", "Madrid", false);
        fixed.put("address", "C. de Cronos, 63 (Doofinder)");

        mvc.perform(put("/api/admin/events/" + typo.getId()).header("Authorization", bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(fixed)))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/events/" + typo.getId()).header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(fixed)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Meetup de React"))
                .andExpect(jsonPath("$.city").value("Madrid"))
                .andExpect(jsonPath("$.address").value("C. de Cronos, 63 (Doofinder)"))
                .andExpect(jsonPath("$.authorName").value("Ana"));

        // Si pasa a online, se quedan sin ciudad ni dirección.
        Map<String, Object> online = event("Meetup de React", TODAY.plusDays(2), "ONLINE", "Madrid", false);
        mvc.perform(put("/api/admin/events/" + typo.getId()).header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(online)))
                .andExpect(jsonPath("$.city").value(nullValue()))
                .andExpect(jsonPath("$.address").value(nullValue()));
    }

    @Test
    void sharingNeedsAnAccountWithAVerifiedEmail() throws Exception {
        mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(event("Sin sesión", TODAY, "ONLINE", null, false))))
                .andExpect(status().isUnauthorized());
        User unverified = user("Sin verificar", User.Role.USER, false);
        create(unverified, event("Charla de Python", TODAY, "ONLINE", null, false)).andExpect(status().isForbidden());
    }

    @Test
    void rejectsUnsafeLinksPastDatesAndInPersonEventsWithoutCity() throws Exception {
        User ana = user("Ana", User.Role.USER, true);
        Map<String, Object> plainHttp = event("Charla de Python", TODAY, "ONLINE", null, false);
        plainHttp.put("url", "http://inseguro.example.com/evento");
        create(ana, plainHttp).andExpect(status().isBadRequest());

        Map<String, Object> script = event("Charla de Python", TODAY, "ONLINE", null, false);
        script.put("url", "javascript:alert(1)");
        create(ana, script).andExpect(status().isBadRequest());

        create(ana, event("Charla de Python", TODAY.minusDays(1), "ONLINE", null, false)).andExpect(status().isBadRequest());
        create(ana, event("Taller presencial", TODAY, "PRESENCIAL", " ", false))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Indica la ciudad del evento presencial"));
        assertThat(eventRepository.count()).isZero();
    }

    @Test
    void theSameEventCannotBeSharedTwiceAndThereIsADailyLimit() throws Exception {
        User ana = user("Ana", User.Role.USER, true);
        Map<String, Object> meetup = event("Meetup de React", TODAY.plusDays(2), "ONLINE", null, false);
        create(ana, meetup).andExpect(status().isCreated());
        create(user("Bea", User.Role.USER, true), meetup)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ese evento ya está en la lista"));

        for (int i = 1; i < 5; i++) {
            Map<String, Object> other = event("Evento número " + i, TODAY.plusDays(2), "ONLINE", null, false);
            other.put("url", "https://www.meetup.com/grupo/events/" + i);
            create(ana, other).andExpect(status().isCreated());
        }
        Map<String, Object> sixth = event("Evento número seis", TODAY.plusDays(2), "ONLINE", null, false);
        sixth.put("url", "https://www.meetup.com/grupo/events/6");
        create(ana, sixth).andExpect(status().isConflict());
    }

    @Test
    void whoSharedItOrAnAdministratorCanDeleteItButNobodyElse() throws Exception {
        User ana = user("Ana", User.Role.USER, true);
        User bea = user("Bea", User.Role.USER, true);
        User admin = user("Admin", User.Role.ADMIN, true);
        CommunityEvent first = saved(ana, "Primero", TODAY.plusDays(1), EventMode.ONLINE, false, true);
        CommunityEvent repeated = saved(ana, "Repetido", TODAY.plusDays(1), EventMode.ONLINE, false, true);

        mvc.perform(delete("/api/events/" + first.getId()).header("Authorization", bearer(bea)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/events/" + first.getId()).header("Authorization", bearer(ana)))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/events/" + repeated.getId()).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
        assertThat(eventRepository.count()).isZero();
    }

    @Test
    void aReportedEventCanBeHiddenFromTheReportsQueue() throws Exception {
        User ana = user("Ana", User.Role.USER, true);
        User bea = user("Bea", User.Role.USER, true);
        User admin = user("Admin", User.Role.ADMIN, true);
        CommunityEvent spam = saved(ana, "Compra seguidores baratos", TODAY.plusDays(1), EventMode.ONLINE, false, true);

        mvc.perform(post("/api/reports").header("Authorization", bearer(bea)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"EVENT\",\"targetId\":" + spam.getId() + ",\"reason\":\"SPAM\"}"))
                .andExpect(status().is2xxSuccessful());
        mvc.perform(get("/api/admin/reports").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$[0].targetType").value("EVENT"))
                .andExpect(jsonPath("$[0].target.title").value("Compra seguidores baratos"))
                .andExpect(jsonPath("$[0].target.authorName").value("Ana"));
        mvc.perform(post("/api/admin/reports/resolve").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"EVENT\",\"targetId\":" + spam.getId() + ",\"action\":\"HIDE\"}"))
                .andExpect(status().is2xxSuccessful());

        mvc.perform(get("/api/events")).andExpect(jsonPath("$.content", hasSize(0)));
        // En el panel sigue, marcado como oculto, con quién lo compartió.
        mvc.perform(get("/api/admin/events").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.content[0].active").value(false))
                .andExpect(jsonPath("$.content[0].authorName").value("Ana"));
        mvc.perform(get("/api/admin/events").header("Authorization", bearer(ana))).andExpect(status().isForbidden());
    }

    private ResultActions create(User author, Map<String, Object> body) throws Exception {
        return mvc.perform(post("/api/events").header("Authorization", bearer(author))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }

    private static Map<String, Object> event(String title, LocalDate date, String mode, String city, boolean forWomen) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("url", "https://www.meetup.com/react-valencia/events/123/");
        body.put("title", title);
        body.put("description", "Charlas cortas para empezar con React y conocer a gente de la comunidad.");
        body.put("date", date.toString());
        body.put("time", "19:00");
        body.put("mode", mode);
        body.put("city", city);
        body.put("address", city == null ? null : "Calle Mayor, 1");
        body.put("forWomen", forWomen);
        return body;
    }

    private CommunityEvent saved(User author, String title, LocalDate date, EventMode mode, boolean forWomen,
                                 boolean active) {
        return saved(author, title, date, mode, forWomen, active, "Madrid");
    }

    private CommunityEvent saved(User author, String title, LocalDate date, EventMode mode, boolean forWomen,
                                 boolean active, String city) {
        CommunityEvent event = new CommunityEvent();
        event.setTitle(title);
        event.setDescription("Descripción de prueba del evento.");
        event.setUrl("https://www.meetup.com/" + UUID.randomUUID());
        event.setStartsOn(date);
        event.setMode(mode);
        event.setCity(mode == EventMode.PRESENCIAL ? city : null);
        event.setForWomen(forWomen);
        event.setActive(active);
        event.setAuthor(author);
        return eventRepository.save(event);
    }

    private EmailMessage awaitEmail() throws InterruptedException {
        for (int i = 0; i < 50 && mail.sent.isEmpty(); i++) {
            Thread.sleep(100);
        }
        assertThat(mail.sent).as("aviso de evento nuevo").isNotEmpty();
        return mail.sent.get(0);
    }

    private User user(String name, User.Role role, boolean verified) {
        User user = new User();
        user.setName(name);
        user.setEmail("event-" + UUID.randomUUID() + "@test.com");
        user.setRole(role);
        user.setEnabled(true);
        user.setEmailVerifiedAt(verified ? LocalDateTime.now() : null);
        return userRepository.save(user);
    }

    private String bearer(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
    }
}
