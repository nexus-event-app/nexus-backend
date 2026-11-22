package com.nexus.backend.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.CommunityEvent;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.EventMode;
import com.nexus.backend.mail.EmailMessage;
import com.nexus.backend.repository.CommunityEventRepository;
import com.nexus.backend.repository.EventInterestRepository;
import com.nexus.backend.repository.ReportRepository;
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
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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

@Import(CapturingMail.class)
@SpringBootTest(properties = {"app.moderation.email=moderacion@primercommit.test", "app.auth.require-verified-email=true"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventIntegrationTest {

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
    private TestUsers users;
    @Autowired
    private CapturingMail.Sender mail;
    @Autowired
    private EventInterestRepository interestRepository;

    @BeforeEach
    void clean() {
        reportRepository.deleteAll();
        interestRepository.deleteAll();
        eventRepository.deleteAll();
        mail.sent.clear();
    }

    @Test
    void sharingAnEventPublishesItAtOnceWithoutTheAuthorAndEmailsModeration() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);

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
        User ana = users.create("Ana", User.Role.USER, true);
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
        User ana = users.create("Ana", User.Role.USER, true);
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
        mvc.perform(get("/api/events/cities")).andExpect(jsonPath("$", contains("Barcelona", "Madrid")));
    }

    @Test
    void theCalendarMarksTheDaysWithEventsAndWhetherAnyIsForWomen() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
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
        User ana = users.create("Ana", User.Role.USER, true);
        User admin = users.create("Admin", User.Role.ADMIN, true);
        CommunityEvent typo = saved(ana, "Meetup de Reakt", TODAY.plusDays(1), EventMode.PRESENCIAL, false, true,
                "Doofinder C. de Cronos, 63 · Madrid");
        Map<String, Object> fixed = event("Meetup de React", TODAY.plusDays(2), "PRESENCIAL", "Madrid", false);
        fixed.put("address", "C. de Cronos, 63 (Doofinder)");

        mvc.perform(put("/api/admin/events/" + typo.getId()).header("Authorization", users.bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(fixed)))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/events/" + typo.getId()).header("Authorization", users.bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(fixed)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Meetup de React"))
                .andExpect(jsonPath("$.city").value("Madrid"))
                .andExpect(jsonPath("$.address").value("C. de Cronos, 63 (Doofinder)"))
                .andExpect(jsonPath("$.authorName").value("Ana"));

        Map<String, Object> online = event("Meetup de React", TODAY.plusDays(2), "ONLINE", "Madrid", false);
        mvc.perform(put("/api/admin/events/" + typo.getId()).header("Authorization", users.bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(online)))
                .andExpect(jsonPath("$.city").value(nullValue()))
                .andExpect(jsonPath("$.address").value(nullValue()));
    }

    @Test
    void sharingNeedsAnAccountWithAVerifiedEmail() throws Exception {
        mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(event("Sin sesión", TODAY, "ONLINE", null, false))))
                .andExpect(status().isUnauthorized());
        User unverified = users.create("Sin verificar", User.Role.USER, false);
        create(unverified, event("Charla de Python", TODAY, "ONLINE", null, false)).andExpect(status().isForbidden());
    }

    @Test
    void rejectsUnsafeLinksPastDatesAndInPersonEventsWithoutCity() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
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
        User ana = users.create("Ana", User.Role.USER, true);
        Map<String, Object> meetup = event("Meetup de React", TODAY.plusDays(2), "ONLINE", null, false);
        create(ana, meetup).andExpect(status().isCreated());
        create(users.create("Bea", User.Role.USER, true), meetup)
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
        User ana = users.create("Ana", User.Role.USER, true);
        User bea = users.create("Bea", User.Role.USER, true);
        User admin = users.create("Admin", User.Role.ADMIN, true);
        CommunityEvent first = saved(ana, "Primero", TODAY.plusDays(1), EventMode.ONLINE, false, true);
        CommunityEvent repeated = saved(ana, "Repetido", TODAY.plusDays(1), EventMode.ONLINE, false, true);

        mvc.perform(delete("/api/events/" + first.getId()).header("Authorization", users.bearer(bea)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/events/" + first.getId()).header("Authorization", users.bearer(ana)))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/events/" + repeated.getId()).header("Authorization", users.bearer(admin)))
                .andExpect(status().isNoContent());
        assertThat(eventRepository.count()).isZero();
    }

    @Test
    void aReportedEventCanBeHiddenFromTheReportsQueue() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
        User bea = users.create("Bea", User.Role.USER, true);
        User admin = users.create("Admin", User.Role.ADMIN, true);
        CommunityEvent spam = saved(ana, "Compra seguidores baratos", TODAY.plusDays(1), EventMode.ONLINE, false, true);

        mvc.perform(post("/api/reports").header("Authorization", users.bearer(bea)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"EVENT\",\"targetId\":" + spam.getId() + ",\"reason\":\"SPAM\"}"))
                .andExpect(status().is2xxSuccessful());
        mvc.perform(get("/api/admin/reports").header("Authorization", users.bearer(admin)))
                .andExpect(jsonPath("$[0].targetType").value("EVENT"))
                .andExpect(jsonPath("$[0].target.title").value("Compra seguidores baratos"))
                .andExpect(jsonPath("$[0].target.authorName").value("Ana"));
        mvc.perform(post("/api/admin/reports/resolve").header("Authorization", users.bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"EVENT\",\"targetId\":" + spam.getId() + ",\"action\":\"HIDE\"}"))
                .andExpect(status().is2xxSuccessful());

        mvc.perform(get("/api/events")).andExpect(jsonPath("$.content", hasSize(0)));
        mvc.perform(get("/api/admin/events").header("Authorization", users.bearer(admin)))
                .andExpect(jsonPath("$.content[0].active").value(false))
                .andExpect(jsonPath("$.content[0].authorName").value("Ana"));
        mvc.perform(get("/api/admin/events").header("Authorization", users.bearer(ana))).andExpect(status().isForbidden());
    }

    @Test
    void whenTheCityCarriesTheAddressOnlyTheCityIsKeptAndTheRestGoesToTheAddress() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
        Map<String, Object> pasted = event("Meetup de Java", TODAY.plusDays(2), "PRESENCIAL",
                "Doofinder C. de Cronos, 63 · Madrid", false);
        pasted.put("address", null);
        create(ana, pasted)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.city").value("Madrid"))
                .andExpect(jsonPath("$.address").value("Doofinder C. de Cronos, 63 · Madrid"));

        Map<String, Object> withAddress = event("Taller de Git", TODAY.plusDays(3), "PRESENCIAL",
                "Calle Pelai 12, Barcelona", false);
        withAddress.put("url", "https://www.meetup.com/git/events/1/");
        create(ana, withAddress)
                .andExpect(jsonPath("$.city").value("Barcelona"))
                .andExpect(jsonPath("$.address").value("Calle Mayor, 1"));

        mvc.perform(get("/api/events/cities")).andExpect(jsonPath("$", contains("Barcelona", "Madrid")));
    }

    @Test
    void longDescriptionsAreKeptWholeUpTo2000Characters() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
        String whole = ("Los agentes se han convertido en el nuevo punto de partida de la IA. En este taller aprenderás "
                + "los conceptos fundamentales: el razonamiento, el uso de herramientas y la memoria.\n\n").repeat(6).trim();
        assertThat(whole.length()).isGreaterThan(280).isLessThan(2000);
        Map<String, Object> body = event("Agentes de IA de la A a la Z", TODAY.plusDays(4), "ONLINE", null, false);
        body.put("description", whole);
        create(ana, body).andExpect(status().isCreated()).andExpect(jsonPath("$.description").value(whole));
        mvc.perform(get("/api/events")).andExpect(jsonPath("$.content[0].description").value(whole));

        Map<String, Object> tooLong = event("Evento demasiado largo", TODAY.plusDays(5), "ONLINE", null, false);
        tooLong.put("url", "https://www.meetup.com/largo/events/1/");
        tooLong.put("description", "a".repeat(2001));
        create(ana, tooLong).andExpect(status().isBadRequest());
    }

    @Test
    void meApuntoShowsHowManyPeopleAreInterestedWithoutSayingWho() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
        User bea = users.create("Bea", User.Role.USER, true);
        User carla = users.create("Carla", User.Role.USER, true);
        CommunityEvent meetup = saved(ana, "Meetup", TODAY.plusDays(2), EventMode.ONLINE, false, true);
        String interest = "/api/events/" + meetup.getId() + "/interest";

        mvc.perform(post(interest)).andExpect(status().isUnauthorized());
        mvc.perform(post(interest).header("Authorization", users.bearer(bea)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.interested").value(true));
        mvc.perform(post(interest).header("Authorization", users.bearer(carla)))
                .andExpect(jsonPath("$.count").value(2));

        mvc.perform(get("/api/events").header("Authorization", users.bearer(bea)))
                .andExpect(jsonPath("$.content[0].interestedCount").value(2))
                .andExpect(jsonPath("$.content[0].interested").value(true));
        mvc.perform(get("/api/events"))
                .andExpect(jsonPath("$.content[0].interestedCount").value(2))
                .andExpect(jsonPath("$.content[0].interested").value(false))
                .andExpect(jsonPath("$.content[0].interestedUsers").doesNotExist());

        mvc.perform(post(interest).header("Authorization", users.bearer(bea)))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.interested").value(false));
        mvc.perform(get("/api/events").header("Authorization", users.bearer(bea)))
                .andExpect(jsonPath("$.content[0].interestedCount").value(1))
                .andExpect(jsonPath("$.content[0].interested").value(false));
    }

    @Test
    void meApuntoOnlyWorksOnVisibleEventsAndWithAVerifiedAccount() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
        CommunityEvent hidden = saved(ana, "Oculto", TODAY.plusDays(1), EventMode.ONLINE, false, false);
        CommunityEvent past = saved(ana, "Pasado", TODAY.minusDays(1), EventMode.ONLINE, false, true);
        CommunityEvent open = saved(ana, "Abierto", TODAY.plusDays(1), EventMode.ONLINE, false, true);

        mvc.perform(post("/api/events/" + hidden.getId() + "/interest").header("Authorization", users.bearer(ana)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/events/" + past.getId() + "/interest").header("Authorization", users.bearer(ana)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/events/" + open.getId() + "/interest")
                        .header("Authorization", users.bearer(users.create("Sin verificar", User.Role.USER, false))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/events/" + open.getId() + "/interest").header("Authorization", users.bearer(ana)))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/events/" + open.getId()).header("Authorization", users.bearer(ana)))
                .andExpect(status().isNoContent());
        assertThat(interestRepository.count()).isZero();
    }

    private ResultActions create(User author, Map<String, Object> body) throws Exception {
        return mvc.perform(post("/api/events").header("Authorization", users.bearer(author))
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
}
