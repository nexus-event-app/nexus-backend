package com.nexus.backend.security;

import com.nexus.backend.entity.Comment;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.repository.CommentRepository;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Propiedad del contenido del foro: solo el autor o un administrador pueden editar o borrar,
 * y sin sesión no se puede escribir. Los usuarios y el contenido se insertan directamente en BD
 * para no consumir los límites de peticiones por IP.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContentOwnershipIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiscussionRepository discussionRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void writingToTheForumRequiresLogin() throws Exception {
        mvc.perform(post("/api/discussions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hola mundo\",\"content\":\"Contenido de prueba\",\"category\":\"GENERAL\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/comments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"discussionId\":1,\"content\":\"Hola\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/testimonials").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"content\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void onlyTheAuthorOrAnAdminCanDeleteADiscussion() throws Exception {
        User author = user(User.Role.USER);
        User stranger = user(User.Role.USER);
        User admin = user(User.Role.ADMIN);

        Long first = discussion(author).getId();
        mvc.perform(delete("/api/discussions/" + first).header("Authorization", bearer(stranger)))
                .andExpect(status().isForbidden());
        assertThat(discussionRepository.existsById(first)).isTrue();

        mvc.perform(delete("/api/discussions/" + first).header("Authorization", bearer(author)))
                .andExpect(status().isNoContent());
        assertThat(discussionRepository.existsById(first)).isFalse();

        Long second = discussion(author).getId();
        mvc.perform(delete("/api/discussions/" + second).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
        assertThat(discussionRepository.existsById(second)).isFalse();
    }

    @Test
    void onlyTheAuthorOrAnAdminCanDeleteAComment() throws Exception {
        User author = user(User.Role.USER);
        User stranger = user(User.Role.USER);
        User admin = user(User.Role.ADMIN);
        Discussion discussion = discussion(author);

        Long first = comment(discussion, author).getId();
        mvc.perform(delete("/api/comments/" + first).header("Authorization", bearer(stranger)))
                .andExpect(status().isForbidden());
        assertThat(commentRepository.existsById(first)).isTrue();

        mvc.perform(delete("/api/comments/" + first).header("Authorization", bearer(author)))
                .andExpect(status().isNoContent());
        assertThat(commentRepository.existsById(first)).isFalse();

        Long second = comment(discussion, author).getId();
        mvc.perform(delete("/api/comments/" + second).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
        assertThat(commentRepository.existsById(second)).isFalse();
    }

    @Test
    void aStrangerCannotEditSomeoneElsesDiscussion() throws Exception {
        Long id = discussion(user(User.Role.USER)).getId();

        mvc.perform(put("/api/discussions/" + id).header("Authorization", bearer(user(User.Role.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Título nuevo\",\"content\":\"Contenido nuevo largo\",\"category\":\"GENERAL\"}"))
                .andExpect(status().isForbidden());
    }

    private User user(User.Role role) {
        User user = new User();
        user.setName("Usuario de prueba");
        user.setEmail("u-" + UUID.randomUUID() + "@test.com");
        user.setPassword(passwordEncoder.encode("Clave12345"));
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private String bearer(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
    }

    private Discussion discussion(User author) {
        Discussion discussion = new Discussion();
        discussion.setTitle("Discusión de prueba");
        discussion.setContent("Contenido de la discusión de prueba");
        discussion.setCategory(DiscussionCategory.GENERAL);
        discussion.setAuthor(author);
        return discussionRepository.save(discussion);
    }

    private Comment comment(Discussion discussion, User author) {
        Comment comment = new Comment();
        comment.setContent("Comentario de prueba");
        comment.setDiscussion(discussion);
        comment.setAuthor(author);
        return commentRepository.save(comment);
    }
}
