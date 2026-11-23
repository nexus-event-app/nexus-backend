package com.nexus.backend.moderation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.User;
import com.nexus.backend.repository.CommunityEventRepository;
import com.nexus.backend.repository.DiscussionRepository;
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
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.moderation.email=moderacion@primercommit.test")
@Import(CapturingMail.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ModerationIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private TestUsers users;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private DiscussionRepository discussionRepository;
    @Autowired
    private CommunityEventRepository eventRepository;
    @Autowired
    private CapturingMail.Sender mail;

    private User admin;
    private User ana;
    private User luis;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        mail.sent.clear();
        admin = users.create("Admin", User.Role.ADMIN, true);
        ana = users.create("Ana", User.Role.USER, true);
        luis = users.create("Luis", User.Role.USER, true);
    }

    @Test
    void losInsultosGravesNoSePublican() throws Exception {
        discussion(ana, "Pregunta", "Eres un subnormal").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("no están permitidas")));
        long id = id(discussion(luis, "Duda sobre Git", "¿Cómo deshago un commit?").andExpect(status().isCreated()));
        comment(ana, id, "Calla, maricón").andExpect(status().isBadRequest());
        discussion(admin, "Aviso", "Hijo de puta").andExpect(status().isBadRequest());
        assertThat(reportRepository.count()).isZero();
    }

    @Test
    void unaDiscusionConPalabrotasEsperaRevisionYSePublicaAlDescartar() throws Exception {
        JsonNode created = body(discussion(ana, "Qué mierda de error", "No me compila nada").andExpect(status().isCreated()));
        long id = created.get("id").asLong();
        assertThat(created.get("held").asBoolean()).isTrue();

        mvc.perform(get("/api/discussions/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/discussions/" + id).header("Authorization", users.bearer(luis))).andExpect(status().isNotFound());
        mvc.perform(get("/api/discussions/" + id).header("Authorization", users.bearer(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.held").value(true));

        mvc.perform(get("/api/admin/reports").header("Authorization", users.bearer(admin)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].reasons.LANGUAGE").value(1))
                .andExpect(jsonPath("$[0].target.held").value(true))
                .andExpect(jsonPath("$[0].reports[0].reporterName").doesNotExist())
                .andExpect(jsonPath("$[0].reports[0].details", containsString("mierda")));
        assertThat(mail.sent).anySatisfy(message -> assertThat(message.subject()).contains("Qué mierda de error"));

        resolve("DISCUSSION", id, "DISMISS").andExpect(status().isNoContent());
        mvc.perform(get("/api/discussions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.held").value(false));
    }

    @Test
    void unComentarioRetenidoSoloLoVeSuAutoraHastaQueSeRevisa() throws Exception {
        long id = id(discussion(luis, "Primer empleo", "¿Cómo preparo la entrevista?").andExpect(status().isCreated()));
        comment(ana, id, "Joder, qué nervios").andExpect(status().isCreated()).andExpect(jsonPath("$.held").value(true));
        comment(luis, id, "¡Mucho ánimo!").andExpect(status().isCreated()).andExpect(jsonPath("$.held").value(false));

        mvc.perform(get("/api/comments/discussion/" + id)).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/comments/discussion/" + id).header("Authorization", users.bearer(ana)))
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/discussions/" + id)).andExpect(jsonPath("$.commentCount").value(1));
    }

    @Test
    void laAdministracionPublicaSinEsperarPeroTampocoPuedeInsultar() throws Exception {
        discussion(admin, "Joder, qué semana", "Resumen de novedades").andExpect(status().isCreated())
                .andExpect(jsonPath("$.held").value(false));
        assertThat(reportRepository.count()).isZero();
    }

    @Test
    void unEventoConPalabrotasNoSaleEnLaAgendaHastaRevisarlo() throws Exception {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("url", "https://www.meetup.com/js/events/1/");
        event.put("title", "Quedada de JavaScript de puta madre");
        event.put("description", "Charlas cortas y networking.");
        event.put("date", LocalDate.now().plusDays(10).toString());
        event.put("mode", "ONLINE");
        long id = id(mvc.perform(post("/api/events").header("Authorization", users.bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.held").value(true)));
        assertThat(eventRepository.findById(id).orElseThrow().isActive()).isFalse();
    }

    @Test
    void elPerfilYElNombreNoAdmitenPalabrotas() throws Exception {
        mvc.perform(put("/api/profile").header("Authorization", users.bearer(ana)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"headline\":\"Programadora de mierda\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("no están permitidas")));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Gilipollas\",\"email\":\"nuevo-filtro@test.com\",\"password\":\"Clave12345\","
                                + "\"acceptTerms\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tresReportesDePersonasDistintasOcultanLaDiscusionHastaRevisarla() throws Exception {
        long id = id(discussion(ana, "Compra seguidores", "Visita mi web").andExpect(status().isCreated()));
        report(luis, id).andExpect(status().isNoContent());
        report(users.create("Eva", User.Role.USER, true), id).andExpect(status().isNoContent());
        mvc.perform(get("/api/discussions/" + id)).andExpect(status().isOk());

        report(users.create("Marta", User.Role.USER, true), id).andExpect(status().isNoContent());
        mvc.perform(get("/api/discussions/" + id)).andExpect(status().isNotFound());
        assertThat(discussionRepository.findById(id).orElseThrow().isHeld()).isTrue();
        assertThat(mail.sent).anySatisfy(message -> assertThat(message.subject()).contains("Compra seguidores"));

        resolve("DISCUSSION", id, "DISMISS").andExpect(status().isNoContent());
        mvc.perform(get("/api/discussions/" + id)).andExpect(status().isOk());
    }

    @Test
    void nadiePuedeReportarConElMotivoReservadoAlFiltro() throws Exception {
        long id = id(discussion(ana, "Duda de estudio", "¿Qué estudio primero?").andExpect(status().isCreated()));
        mvc.perform(post("/api/reports").header("Authorization", users.bearer(luis)).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("targetType", "DISCUSSION", "targetId", id,
                                "reason", "LANGUAGE"))))
                .andExpect(status().isBadRequest());
    }

    private ResultActions discussion(User author, String title, String content) throws Exception {
        return mvc.perform(post("/api/discussions").header("Authorization", users.bearer(author))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("title", title, "content", content, "category", "GENERAL"))));
    }

    private ResultActions comment(User author, long discussionId, String content) throws Exception {
        return mvc.perform(post("/api/comments").header("Authorization", users.bearer(author))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("discussionId", discussionId, "content", content))));
    }

    private ResultActions report(User reporter, long id) throws Exception {
        return mvc.perform(post("/api/reports").header("Authorization", users.bearer(reporter))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("targetType", "DISCUSSION", "targetId", id, "reason", "SPAM"))));
    }

    private ResultActions resolve(String type, long id, String action) throws Exception {
        return mvc.perform(post("/api/admin/reports/resolve").header("Authorization", users.bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("targetType", type, "targetId", id, "action", action))));
    }

    private JsonNode body(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private long id(ResultActions result) throws Exception {
        return body(result).get("id").asLong();
    }

    @Test
    void laAdministracionVeLoRetenidoParaPoderRevisarlo() throws Exception {
        long held = id(discussion(ana, "Qué mierda de commit", "No entiendo el error").andExpect(status().isCreated()));
        long visible = id(discussion(ana, "Duda sobre ramas", "¿Cuándo uso rebase?").andExpect(status().isCreated()));
        comment(luis, visible, "Joder, a mí también me pasa").andExpect(status().isCreated());

        mvc.perform(get("/api/discussions/" + held).header("Authorization", users.bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.held").value(true));
        mvc.perform(get("/api/comments/discussion/" + visible).header("Authorization", users.bearer(admin)))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/comments/discussion/" + visible)).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/admin/stats").header("Authorization", users.bearer(admin)))
                .andExpect(jsonPath("$.pendingReports").value(2));
    }
}
