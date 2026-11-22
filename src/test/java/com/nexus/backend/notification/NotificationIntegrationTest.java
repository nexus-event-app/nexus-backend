package com.nexus.backend.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.support.TestUsers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiscussionRepository discussionRepository;
    @Autowired
    private TestUsers users;

    @Test
    void requiresLogin() throws Exception {
        mvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/notifications/unread-count")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/notifications/read-all")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/discussions/1/accepted-answer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"commentId\":1}")).andExpect(status().isUnauthorized());
    }

    @Test
    void theAuthorIsNotifiedOfRepliesButNotOfTheirOwn() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
        User luis = users.create("Luis", User.Role.USER, true);
        Discussion discussion = discussion(ana, "¿Cómo empiezo con Spring?");

        comment(luis, discussion, "Empieza por Spring Initializr.");
        comment(ana, discussion, "¡Gracias, lo pruebo!");

        mvc.perform(get("/api/notifications/unread-count").header("Authorization", users.bearer(ana)))
                .andExpect(jsonPath("$.count").value(1));
        mvc.perform(get("/api/notifications").header("Authorization", users.bearer(ana)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].type").value("COMMENT"))
                .andExpect(jsonPath("$.content[0].actorName").value("Luis"))
                .andExpect(jsonPath("$.content[0].discussionId").value(discussion.getId()))
                .andExpect(jsonPath("$.content[0].discussionSlug").value("como-empiezo-con-spring"))
                .andExpect(jsonPath("$.content[0].read").value(false))
                .andExpect(jsonPath("$.content[0].actorEmail").doesNotExist());
        mvc.perform(get("/api/notifications/unread-count").header("Authorization", users.bearer(luis)))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void marksNotificationsAsReadOnlyForTheirRecipient() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
        User luis = users.create("Luis", User.Role.USER, true);
        Discussion discussion = discussion(ana, "Primera discusión");
        comment(luis, discussion, "Primera respuesta");
        comment(luis, discussion, "Segunda respuesta");
        long first = firstNotificationId(ana);

        mvc.perform(put("/api/notifications/" + first + "/read").header("Authorization", users.bearer(luis)))
                .andExpect(status().isNotFound());

        mvc.perform(put("/api/notifications/" + first + "/read").header("Authorization", users.bearer(ana)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/notifications/unread-count").header("Authorization", users.bearer(ana)))
                .andExpect(jsonPath("$.count").value(1));

        mvc.perform(put("/api/notifications/read-all").header("Authorization", users.bearer(ana)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/notifications/unread-count").header("Authorization", users.bearer(ana)))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void onlyTheAuthorChoosesTheBestAnswerAndItsWriterIsNotified() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
        User luis = users.create("Luis", User.Role.USER, true);
        User admin = users.create("Admin", User.Role.USER, true);
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);
        Discussion discussion = discussion(ana, "Duda con JPA");
        long answer = comment(luis, discussion, "Usa @Transactional en el servicio.");

        String body = "{\"commentId\":" + answer + "}";
        for (User other : new User[]{luis, admin}) {
            mvc.perform(put("/api/discussions/" + discussion.getId() + "/accepted-answer")
                            .header("Authorization", users.bearer(other))
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isForbidden());
        }

        mvc.perform(put("/api/discussions/" + discussion.getId() + "/accepted-answer")
                        .header("Authorization", users.bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acceptedCommentId").value(answer));
        mvc.perform(get("/api/discussions/" + discussion.getId()))
                .andExpect(jsonPath("$.acceptedCommentId").value(answer));
        mvc.perform(get("/api/notifications").header("Authorization", users.bearer(luis)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].type").value("ACCEPTED_ANSWER"))
                .andExpect(jsonPath("$.content[0].commentId").value(answer));

        mvc.perform(put("/api/discussions/" + discussion.getId() + "/accepted-answer")
                .header("Authorization", users.bearer(ana))
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        mvc.perform(get("/api/notifications/unread-count").header("Authorization", users.bearer(luis)))
                .andExpect(jsonPath("$.count").value(1));
        mvc.perform(delete("/api/discussions/" + discussion.getId() + "/accepted-answer")
                        .header("Authorization", users.bearer(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acceptedCommentId").doesNotExist());
        mvc.perform(get("/api/notifications/unread-count").header("Authorization", users.bearer(luis)))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void rejectsCommentsFromAnotherDiscussion() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
        Discussion mine = discussion(ana, "La mía");
        Discussion other = discussion(users.create("Luis", User.Role.USER, true), "La de Luis");
        long foreign = comment(ana, other, "Comentario en otra discusión");

        mvc.perform(put("/api/discussions/" + mine.getId() + "/accepted-answer")
                        .header("Authorization", users.bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"commentId\":" + foreign + "}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/discussions/" + mine.getId() + "/accepted-answer")
                        .header("Authorization", users.bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletingTheBestAnswerReopensTheDiscussionAndRemovesItsNotifications() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
        User luis = users.create("Luis", User.Role.USER, true);
        Discussion discussion = discussion(ana, "Se resolverá y se borrará");
        long answer = comment(luis, discussion, "Esta es la buena.");
        mvc.perform(put("/api/discussions/" + discussion.getId() + "/accepted-answer")
                .header("Authorization", users.bearer(ana))
                .contentType(MediaType.APPLICATION_JSON).content("{\"commentId\":" + answer + "}"));

        mvc.perform(delete("/api/comments/" + answer).header("Authorization", users.bearer(luis)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/discussions/" + discussion.getId()))
                .andExpect(jsonPath("$.acceptedCommentId").doesNotExist());
        mvc.perform(get("/api/notifications").header("Authorization", users.bearer(luis)))
                .andExpect(jsonPath("$.content", hasSize(0)));
        mvc.perform(get("/api/notifications").header("Authorization", users.bearer(ana)))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void hiddenAndDeletedDiscussionsTakeTheirNotificationsWithThem() throws Exception {
        User ana = users.create("Ana", User.Role.USER, true);
        User luis = users.create("Luis", User.Role.USER, true);
        Discussion hidden = discussion(ana, "Se ocultará");
        Discussion deleted = discussion(ana, "Se borrará");
        comment(luis, hidden, "Respuesta a la que se ocultará");
        comment(luis, deleted, "Respuesta a la que se borrará");

        hidden.setIsActive(false);
        discussionRepository.save(hidden);
        mvc.perform(delete("/api/discussions/" + deleted.getId()).header("Authorization", users.bearer(ana)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/notifications/unread-count").header("Authorization", users.bearer(ana)))
                .andExpect(jsonPath("$.count").value(0));
        mvc.perform(get("/api/notifications").header("Authorization", users.bearer(ana)))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    private long comment(User author, Discussion discussion, String content) throws Exception {
        String response = mvc.perform(post("/api/comments").header("Authorization", users.bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("discussionId", discussion.getId(), "content", content))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long firstNotificationId(User recipient) throws Exception {
        String response = mvc.perform(get("/api/notifications").header("Authorization", users.bearer(recipient)))
                .andReturn().getResponse().getContentAsString();
        JsonNode content = objectMapper.readTree(response).get("content");
        return content.get(content.size() - 1).get("id").asLong();
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
}
