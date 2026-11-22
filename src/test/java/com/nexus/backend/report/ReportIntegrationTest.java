package com.nexus.backend.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.Testimonial;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.ReportRepository;
import com.nexus.backend.repository.TestimonialRepository;
import com.nexus.backend.support.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DiscussionRepository discussionRepository;
    @Autowired
    private TestimonialRepository testimonialRepository;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private TestUsers users;

    private User admin;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        admin = users.create("Admin", User.Role.ADMIN, true);
    }

    @Test
    void requiresLoginAndOnlyAdminsSeeTheQueue() throws Exception {
        mvc.perform(post("/api/reports").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/reports").header("Authorization", users.bearer(users.create("Ana", User.Role.USER, true))))
                .andExpect(status().isForbidden());
    }

    @Test
    void reportsAreGroupedByContentWithTheirReasons() throws Exception {
        User author = users.create("Autor", User.Role.USER, true);
        Discussion discussion = discussion(author, "Compra seguidores aquí");

        report(users.create("Ana", User.Role.USER, true), "DISCUSSION", discussion.getId(), "SPAM", "Es publicidad")
                .andExpect(status().isNoContent());
        report(users.create("Luis", User.Role.USER, true), "DISCUSSION", discussion.getId(), "SPAM", null)
                .andExpect(status().isNoContent());
        report(users.create("Marta", User.Role.USER, true), "DISCUSSION", discussion.getId(), "OFFENSIVE", "  ")
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/admin/reports").header("Authorization", users.bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].targetType").value("DISCUSSION"))
                .andExpect(jsonPath("$[0].total").value(3))
                .andExpect(jsonPath("$[0].reasons.SPAM").value(2))
                .andExpect(jsonPath("$[0].reasons.OFFENSIVE").value(1))
                .andExpect(jsonPath("$[0].target.title").value("Compra seguidores aquí"))
                .andExpect(jsonPath("$[0].target.authorName").value("Autor"))
                .andExpect(jsonPath("$[0].target.discussionSlug").value("compra-seguidores-aqui"))
                .andExpect(jsonPath("$[0].reports", hasSize(3)))
                .andExpect(jsonPath("$[0].reports[2].details").value("Es publicidad"))
                .andExpect(jsonPath("$[0].reports[0].details").doesNotExist());
    }

    @Test
    void rejectsOwnContentDuplicatesAndHiddenContent() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
        Discussion own = discussion(ana, "Mi propia discusión");
        Discussion other = discussion(users.create("Luis", User.Role.USER, true), "Discusión de Luis");
        Discussion hidden = discussion(users.create("Eva", User.Role.USER, true), "Oculta");
        hidden.setIsActive(false);
        discussionRepository.save(hidden);

        report(ana, "DISCUSSION", own.getId(), "SPAM", null).andExpect(status().isBadRequest());
        report(ana, "DISCUSSION", other.getId(), "SPAM", null).andExpect(status().isNoContent());
        report(ana, "DISCUSSION", other.getId(), "OTHER", null).andExpect(status().isConflict());
        report(ana, "DISCUSSION", hidden.getId(), "SPAM", null).andExpect(status().isNotFound());
        report(ana, "COMMENT", 999_999L, "SPAM", null).andExpect(status().isNotFound());
        report(ana, "DISCUSSION", other.getId(), "NO_EXISTE", null).andExpect(status().isBadRequest());
        report(ana, "DISCUSSION", other.getId(), "OTHER", "x".repeat(501)).andExpect(status().isBadRequest());
    }

    @Test
    void hidingADiscussionClosesItsReportsAndTakesItOffline() throws Exception {
        Discussion discussion = discussion(users.create("Autor", User.Role.USER, true), "Insultos");
        report(users.create("Ana", User.Role.USER, true), "DISCUSSION", discussion.getId(), "OFFENSIVE", null);

        resolve("DISCUSSION", discussion.getId(), "HIDE").andExpect(status().isNoContent());

        assertThat(discussionRepository.findById(discussion.getId()).orElseThrow().getIsActive()).isFalse();
        mvc.perform(get("/api/discussions/" + discussion.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/admin/reports").header("Authorization", users.bearer(admin))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/admin/reports?status=closed").header("Authorization", users.bearer(admin)))
                .andExpect(jsonPath("$[0].reports[0].status").value("RESOLVED"))
                .andExpect(jsonPath("$[0].reports[0].resolution").value("HIDE"))
                .andExpect(jsonPath("$[0].target.visible").value(false));
        resolve("DISCUSSION", discussion.getId(), "DISMISS").andExpect(status().isNotFound());
    }

    @Test
    void deletingAndDismissingCloseTheReports() throws Exception {
        User author = users.create("Autor", User.Role.USER, true);
        Discussion discussion = discussion(author, "Con un comentario reportado");
        long comment = comment(author, discussion, "Mi número de teléfono es 600000000");
        Testimonial testimonial = testimonial(users.create("Pablo", User.Role.USER, true));
        User ana = users.create("Ana", User.Role.USER, true);
        report(ana, "COMMENT", comment, "PERSONAL_DATA", null).andExpect(status().isNoContent());
        report(ana, "TESTIMONIAL", testimonial.getId(), "MISINFORMATION", null).andExpect(status().isNoContent());

        resolve("COMMENT", comment, "HIDE").andExpect(status().isBadRequest());
        resolve("COMMENT", comment, "DELETE").andExpect(status().isNoContent());
        mvc.perform(get("/api/comments/discussion/" + discussion.getId())).andExpect(jsonPath("$", hasSize(0)));

        resolve("TESTIMONIAL", testimonial.getId(), "DISMISS").andExpect(status().isNoContent());
        assertThat(testimonialRepository.findById(testimonial.getId()).orElseThrow().getIsActive()).isTrue();

        mvc.perform(get("/api/admin/reports").header("Authorization", users.bearer(admin))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/admin/stats").header("Authorization", users.bearer(admin)))
                .andExpect(jsonPath("$.pendingReports").value(0));
    }

    @Test
    void deletingContentFromTheForumAlsoClosesItsPendingReports() throws Exception {
        User author = users.create("Autor", User.Role.USER, true);
        Discussion discussion = discussion(author, "La borrará su autor");
        long comment = comment(author, discussion, "Comentario que también se reporta");
        User ana = users.create("Ana", User.Role.USER, true);
        report(ana, "DISCUSSION", discussion.getId(), "SPAM", null);
        report(ana, "COMMENT", comment, "SPAM", null);
        mvc.perform(get("/api/admin/stats").header("Authorization", users.bearer(admin)))
                .andExpect(jsonPath("$.pendingReports").value(2));

        mvc.perform(delete("/api/discussions/" + discussion.getId()).header("Authorization", users.bearer(author)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/admin/reports").header("Authorization", users.bearer(admin))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/admin/reports?status=closed").header("Authorization", users.bearer(admin)))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].target.exists").value(false));
    }

    private ResultActions report(User reporter, String type, Long id, String reason, String details) throws Exception {
        Map<String, Object> body = new java.util.HashMap<>(Map.of("targetType", type, "targetId", id, "reason", reason));
        if (details != null) {
            body.put("details", details);
        }
        return mvc.perform(post("/api/reports").header("Authorization", users.bearer(reporter))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions resolve(String type, Long id, String action) throws Exception {
        return mvc.perform(post("/api/admin/reports/resolve").header("Authorization", users.bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("targetType", type, "targetId", id, "action", action))));
    }

    private long comment(User author, Discussion discussion, String content) throws Exception {
        String response = mvc.perform(post("/api/comments").header("Authorization", users.bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("discussionId", discussion.getId(), "content", content))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Discussion discussion(User author, String title) {
        Discussion discussion = new Discussion();
        discussion.setTitle(title);
        discussion.setContent("Contenido de la discusión de prueba");
        discussion.setCategory(DiscussionCategory.GENERAL);
        discussion.setAuthor(author);
        discussion.setIsActive(true);
        return discussionRepository.save(discussion);
    }

    private Testimonial testimonial(User user) {
        Testimonial testimonial = new Testimonial();
        testimonial.setName(user.getName());
        testimonial.setContent("Gracias a la comunidad encontré mi primer empleo como desarrolladora junior.");
        testimonial.setUser(user);
        testimonial.setIsActive(true);
        testimonial.setIsPending(false);
        return testimonialRepository.save(testimonial);
    }
}
