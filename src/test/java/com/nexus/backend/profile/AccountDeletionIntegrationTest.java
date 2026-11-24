package com.nexus.backend.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.User;
import com.nexus.backend.repository.LearningProgressRepository;
import com.nexus.backend.repository.NotificationRepository;
import com.nexus.backend.repository.TestimonialRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.support.TestUsers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountDeletionIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestimonialRepository testimonialRepository;
    @Autowired
    private LearningProgressRepository learningProgressRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private TestUsers users;

    @Test
    void requiresLogin() throws Exception {
        mvc.perform(delete("/api/profile")).andExpect(status().isUnauthorized());
    }

    @Test
    void deletesPersonalDataAndKeepsTheForumThreadsAnonymized() throws Exception {
        User ana = user("Ana López", User.Role.USER, true);
        User bea = user("Bea Ruiz", User.Role.USER, true);
        String anaToken = users.bearer(ana);

        long discussionId = id(mvc.perform(post("/api/discussions").header("Authorization", anaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"¿Por dónde empiezo con Java?\",\"content\":\"Busco recursos para empezar desde cero.\",\"category\":\"AYUDA_TECNICA\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        comment(bea, discussionId, "Te recomiendo empezar por los fundamentos.");
        comment(ana, discussionId, "¡Gracias! Empiezo por ahí.");
        mvc.perform(post("/api/testimonials").header("Authorization", anaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"role\":\"Desarrolladora junior\",\"content\":\"Gracias a la comunidad encontré mi primer trabajo en tecnología.\"}"))
                .andExpect(status().isCreated());
        mvc.perform(put("/api/learning/progress/frontend/html").header("Authorization", anaToken))
                .andExpect(status().is2xxSuccessful());
        assertThat(notificationRepository.countUnread(ana.getId())).isPositive();

        mvc.perform(delete("/api/profile").header("Authorization", anaToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"" + TestUsers.PASSWORD + "\"}"))
                .andExpect(status().isNoContent());

        User deleted = userRepository.findById(ana.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.isEnabled()).isFalse();
        assertThat(deleted.getName()).isEqualTo("Cuenta eliminada");
        assertThat(deleted.getEmail()).endsWith(".invalid").doesNotContain(ana.getEmail().split("@")[0]);
        assertThat(deleted.getPassword()).isNull();
        assertThat(deleted.getGoogleSubject()).isNull();
        assertThat(deleted.getBio()).isNull();

        assertThat(testimonialRepository.findIdsByUserId(ana.getId())).isEmpty();
        assertThat(learningProgressRepository.countByUserId(ana.getId())).isZero();
        assertThat(notificationRepository.countUnread(ana.getId())).isZero();

        mvc.perform(get("/api/discussions/" + discussionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorName").value("Cuenta eliminada"))
                .andExpect(jsonPath("$.authorDeleted").value(true));
        String viewer = users.bearer(users.create("Marta", User.Role.USER, true));
        mvc.perform(get("/api/users/" + ana.getId()).header("Authorization", viewer)).andExpect(status().isNotFound());

        mvc.perform(get("/api/profile").header("Authorization", anaToken)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"" + ana.getEmail() + "\",\"password\":\"OtraClave123\","
                                + "\"acceptTerms\":true,\"acceptPrivacy\":true}"))
                .andExpect(status().isCreated());
    }

    @Test
    void theAdminPanelCannotReactivateOrEditADeletedAccount() throws Exception {
        User ana = user("Ana", User.Role.USER, true);
        User admin = user("Admin", User.Role.ADMIN, true);
        mvc.perform(delete("/api/profile").header("Authorization", users.bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"" + TestUsers.PASSWORD + "\"}"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/admin/users").param("q", "Cuenta eliminada").header("Authorization", users.bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + ana.getId() + ")].deleted").value(true));
        mvc.perform(put("/api/admin/users/" + ana.getId() + "/status").header("Authorization", users.bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/users/" + ana.getId() + "/profile").header("Authorization", users.bearer(admin)))
                .andExpect(status().isNotFound());
        assertThat(userRepository.findById(ana.getId()).orElseThrow().isEnabled()).isFalse();
    }

    @Test
    void asksForThePasswordWhenTheAccountHasOne() throws Exception {
        User ana = user("Ana", User.Role.USER, true);
        for (String body : new String[]{"{}", "{\"password\":\"NoEsEsta123\"}"}) {
            mvc.perform(delete("/api/profile").header("Authorization", users.bearer(ana))
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La contraseña no es correcta"));
        }
        assertThat(userRepository.findById(ana.getId()).orElseThrow().isDeleted()).isFalse();
    }

    @Test
    void accountsCreatedWithGoogleWithoutPasswordCanBeDeletedWithoutIt() throws Exception {
        User ana = user("Ana", User.Role.USER, false);
        mvc.perform(get("/api/auth/me").header("Authorization", users.bearer(ana)))
                .andExpect(jsonPath("$.hasPassword").value(false));
        mvc.perform(delete("/api/profile").header("Authorization", users.bearer(ana))).andExpect(status().isNoContent());
        assertThat(userRepository.findById(ana.getId()).orElseThrow().isDeleted()).isTrue();
    }

    @Test
    void administratorsCannotDeleteTheirAccount() throws Exception {
        User admin = user("Admin", User.Role.ADMIN, true);
        mvc.perform(delete("/api/profile").header("Authorization", users.bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"" + TestUsers.PASSWORD + "\"}"))
                .andExpect(status().isForbidden());
        assertThat(userRepository.findById(admin.getId()).orElseThrow().isDeleted()).isFalse();
    }

    private void comment(User author, long discussionId, String content) throws Exception {
        mvc.perform(post("/api/comments").header("Authorization", users.bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"discussionId\":" + discussionId + ",\"content\":\"" + content + "\"}"))
                .andExpect(status().isCreated());
    }

    private long id(String body) throws Exception {
        JsonNode node = json.readTree(body);
        return node.get("id").asLong();
    }

    private User user(String name, User.Role role, boolean withPassword) {
        User user = users.create(name, role, true);
        if (withPassword) {
            return user;
        }
        user.setPassword(null);
        return userRepository.save(user);
    }
}
