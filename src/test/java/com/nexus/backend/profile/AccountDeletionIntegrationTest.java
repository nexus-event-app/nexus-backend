package com.nexus.backend.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.User;
import com.nexus.backend.repository.LearningProgressRepository;
import com.nexus.backend.repository.NotificationRepository;
import com.nexus.backend.repository.TestimonialRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Eliminar la propia cuenta: qué se borra, qué se queda anonimizado en el foro y quién puede hacerlo. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountDeletionIntegrationTest {

    private static final String PASSWORD = "Clave12345";

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
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void requiresLogin() throws Exception {
        mvc.perform(delete("/api/profile")).andExpect(status().isUnauthorized());
    }

    @Test
    void deletesPersonalDataAndKeepsTheForumThreadsAnonymized() throws Exception {
        User ana = user("Ana López", User.Role.USER, true);
        User bea = user("Bea Ruiz", User.Role.USER, true);
        String anaToken = bearer(ana);

        // Actividad de Ana: una discusión con respuesta de Bea (que le genera una notificación), una respuesta suya,
        // un testimonio y un paso completado de una ruta.
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
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isNoContent());

        // Sus datos personales ya no están; la fila queda anonimizada.
        User deleted = userRepository.findById(ana.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.isEnabled()).isFalse();
        assertThat(deleted.getName()).isEqualTo("Cuenta eliminada");
        assertThat(deleted.getEmail()).endsWith(".invalid").doesNotContain("profile-");
        assertThat(deleted.getPassword()).isNull();
        assertThat(deleted.getGoogleSubject()).isNull();
        assertThat(deleted.getBio()).isNull();

        // Lo que solo era suyo, borrado.
        assertThat(testimonialRepository.findIdsByUserId(ana.getId())).isEmpty();
        assertThat(learningProgressRepository.countByUserId(ana.getId())).isZero();
        assertThat(notificationRepository.countUnread(ana.getId())).isZero();

        // El hilo sigue en pie, firmado como «Cuenta eliminada», y su perfil ya no existe.
        mvc.perform(get("/api/discussions/" + discussionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorName").value("Cuenta eliminada"))
                .andExpect(jsonPath("$.authorDeleted").value(true));
        mvc.perform(get("/api/users/" + ana.getId())).andExpect(status().isNotFound());

        // La sesión deja de valer al momento, y con su email se puede crear una cuenta nueva.
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
        mvc.perform(delete("/api/profile").header("Authorization", bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/admin/users").param("q", "Cuenta eliminada").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + ana.getId() + ")].deleted").value(true));
        mvc.perform(put("/api/admin/users/" + ana.getId() + "/status").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/users/" + ana.getId() + "/profile").header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
        assertThat(userRepository.findById(ana.getId()).orElseThrow().isEnabled()).isFalse();
    }

    @Test
    void asksForThePasswordWhenTheAccountHasOne() throws Exception {
        User ana = user("Ana", User.Role.USER, true);
        for (String body : new String[]{"{}", "{\"password\":\"NoEsEsta123\"}"}) {
            mvc.perform(delete("/api/profile").header("Authorization", bearer(ana))
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("La contraseña no es correcta"));
        }
        assertThat(userRepository.findById(ana.getId()).orElseThrow().isDeleted()).isFalse();
    }

    @Test
    void accountsCreatedWithGoogleWithoutPasswordCanBeDeletedWithoutIt() throws Exception {
        User ana = user("Ana", User.Role.USER, false);
        mvc.perform(get("/api/auth/me").header("Authorization", bearer(ana)))
                .andExpect(jsonPath("$.hasPassword").value(false));
        mvc.perform(delete("/api/profile").header("Authorization", bearer(ana))).andExpect(status().isNoContent());
        assertThat(userRepository.findById(ana.getId()).orElseThrow().isDeleted()).isTrue();
    }

    @Test
    void administratorsCannotDeleteTheirAccount() throws Exception {
        User admin = user("Admin", User.Role.ADMIN, true);
        mvc.perform(delete("/api/profile").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isForbidden());
        assertThat(userRepository.findById(admin.getId()).orElseThrow().isDeleted()).isFalse();
    }

    private void comment(User author, long discussionId, String content) throws Exception {
        mvc.perform(post("/api/comments").header("Authorization", bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"discussionId\":" + discussionId + ",\"content\":\"" + content + "\"}"))
                .andExpect(status().isCreated());
    }

    private long id(String body) throws Exception {
        JsonNode node = json.readTree(body);
        return node.get("id").asLong();
    }

    private User user(String name, User.Role role, boolean withPassword) {
        User user = new User();
        user.setName(name);
        user.setEmail("profile-" + UUID.randomUUID() + "@test.com");
        user.setPassword(withPassword ? passwordEncoder.encode(PASSWORD) : null);
        user.setRole(role);
        user.setEnabled(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String bearer(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
    }
}
