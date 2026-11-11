package com.nexus.backend.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.Testimonial;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.ReportRepository;
import com.nexus.backend.repository.TestimonialRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Reportes de la comunidad y cola de moderación. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiscussionRepository discussionRepository;
    @Autowired
    private TestimonialRepository testimonialRepository;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    private User admin;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        admin = user("Admin", User.Role.ADMIN);
    }

    @Test
    void requiresLoginAndOnlyAdminsSeeTheQueue() throws Exception {
        mvc.perform(post("/api/reports").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/reports").header("Authorization", bearer(user("Ana", User.Role.USER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void reportsAreGroupedByContentWithTheirReasons() throws Exception {
        User author = user("Autor", User.Role.USER);
        Discussion discussion = discussion(author, "Compra seguidores aquí");

        report(user("Ana", User.Role.USER), "DISCUSSION", discussion.getId(), "SPAM", "Es publicidad")
                .andExpect(status().isNoContent());
        report(user("Luis", User.Role.USER), "DISCUSSION", discussion.getId(), "SPAM", null)
                .andExpect(status().isNoContent());
        report(user("Marta", User.Role.USER), "DISCUSSION", discussion.getId(), "OFFENSIVE", "  ")
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/admin/reports").header("Authorization", bearer(admin)))
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
        User ana = user("Ana", User.Role.USER);
        Discussion own = discussion(ana, "Mi propia discusión");
        Discussion other = discussion(user("Luis", User.Role.USER), "Discusión de Luis");
        Discussion hidden = discussion(user("Eva", User.Role.USER), "Oculta");
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
        Discussion discussion = discussion(user("Autor", User.Role.USER), "Insultos");
        report(user("Ana", User.Role.USER), "DISCUSSION", discussion.getId(), "OFFENSIVE", null);

        resolve("DISCUSSION", discussion.getId(), "HIDE").andExpect(status().isNoContent());

        assertThat(discussionRepository.findById(discussion.getId()).orElseThrow().getIsActive()).isFalse();
        mvc.perform(get("/api/discussions/" + discussion.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/admin/reports").header("Authorization", bearer(admin))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/admin/reports?status=closed").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$[0].reports[0].status").value("RESOLVED"))
                .andExpect(jsonPath("$[0].reports[0].resolution").value("HIDE"))
                .andExpect(jsonPath("$[0].target.visible").value(false));
        // Ya no hay nada pendiente que resolver.
        resolve("DISCUSSION", discussion.getId(), "DISMISS").andExpect(status().isNotFound());
    }

    @Test
    void deletingAndDismissingCloseTheReports() throws Exception {
        User author = user("Autor", User.Role.USER);
        Discussion discussion = discussion(author, "Con un comentario reportado");
        long comment = comment(author, discussion, "Mi número de teléfono es 600000000");
        Testimonial testimonial = testimonial(user("Pablo", User.Role.USER));
        User ana = user("Ana", User.Role.USER);
        report(ana, "COMMENT", comment, "PERSONAL_DATA", null).andExpect(status().isNoContent());
        report(ana, "TESTIMONIAL", testimonial.getId(), "MISINFORMATION", null).andExpect(status().isNoContent());

        // Un comentario no se puede ocultar, solo borrar.
        resolve("COMMENT", comment, "HIDE").andExpect(status().isBadRequest());
        resolve("COMMENT", comment, "DELETE").andExpect(status().isNoContent());
        mvc.perform(get("/api/comments/discussion/" + discussion.getId())).andExpect(jsonPath("$", hasSize(0)));

        resolve("TESTIMONIAL", testimonial.getId(), "DISMISS").andExpect(status().isNoContent());
        assertThat(testimonialRepository.findById(testimonial.getId()).orElseThrow().getIsActive()).isTrue();

        mvc.perform(get("/api/admin/reports").header("Authorization", bearer(admin))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/admin/stats").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.pendingReports").value(0));
    }

    @Test
    void deletingContentFromTheForumAlsoClosesItsPendingReports() throws Exception {
        User author = user("Autor", User.Role.USER);
        Discussion discussion = discussion(author, "La borrará su autor");
        long comment = comment(author, discussion, "Comentario que también se reporta");
        User ana = user("Ana", User.Role.USER);
        report(ana, "DISCUSSION", discussion.getId(), "SPAM", null);
        report(ana, "COMMENT", comment, "SPAM", null);
        mvc.perform(get("/api/admin/stats").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.pendingReports").value(2));

        mvc.perform(delete("/api/discussions/" + discussion.getId()).header("Authorization", bearer(author)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/admin/reports").header("Authorization", bearer(admin))).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/admin/reports?status=closed").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].target.exists").value(false));
    }

    private ResultActions report(User reporter, String type, Long id, String reason, String details) throws Exception {
        Map<String, Object> body = new java.util.HashMap<>(Map.of("targetType", type, "targetId", id, "reason", reason));
        if (details != null) {
            body.put("details", details);
        }
        return mvc.perform(post("/api/reports").header("Authorization", bearer(reporter))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions resolve(String type, Long id, String action) throws Exception {
        return mvc.perform(post("/api/admin/reports/resolve").header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("targetType", type, "targetId", id, "action", action))));
    }

    private long comment(User author, Discussion discussion, String content) throws Exception {
        String response = mvc.perform(post("/api/comments").header("Authorization", bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("discussionId", discussion.getId(), "content", content))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private User user(String name, User.Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail("report-" + UUID.randomUUID() + "@test.com");
        user.setPassword(passwordEncoder.encode("Clave12345"));
        user.setRole(role);
        user.setEnabled(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String bearer(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
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
