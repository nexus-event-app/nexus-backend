package com.nexus.backend.security;

import com.nexus.backend.entity.Comment;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.repository.CommentRepository;
import com.nexus.backend.repository.DiscussionRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContentOwnershipIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private DiscussionRepository discussionRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private TestUsers users;

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
        User author = users.create("Usuario de prueba", User.Role.USER, false);
        User stranger = users.create("Usuario de prueba", User.Role.USER, false);
        User admin = users.create("Usuario de prueba", User.Role.ADMIN, false);

        Long first = discussion(author).getId();
        mvc.perform(delete("/api/discussions/" + first).header("Authorization", users.bearer(stranger)))
                .andExpect(status().isForbidden());
        assertThat(discussionRepository.existsById(first)).isTrue();

        mvc.perform(delete("/api/discussions/" + first).header("Authorization", users.bearer(author)))
                .andExpect(status().isNoContent());
        assertThat(discussionRepository.existsById(first)).isFalse();

        Long second = discussion(author).getId();
        mvc.perform(delete("/api/discussions/" + second).header("Authorization", users.bearer(admin)))
                .andExpect(status().isNoContent());
        assertThat(discussionRepository.existsById(second)).isFalse();
    }

    @Test
    void onlyTheAuthorOrAnAdminCanDeleteAComment() throws Exception {
        User author = users.create("Usuario de prueba", User.Role.USER, false);
        User stranger = users.create("Usuario de prueba", User.Role.USER, false);
        User admin = users.create("Usuario de prueba", User.Role.ADMIN, false);
        Discussion discussion = discussion(author);

        Long first = comment(discussion, author).getId();
        mvc.perform(delete("/api/comments/" + first).header("Authorization", users.bearer(stranger)))
                .andExpect(status().isForbidden());
        assertThat(commentRepository.existsById(first)).isTrue();

        mvc.perform(delete("/api/comments/" + first).header("Authorization", users.bearer(author)))
                .andExpect(status().isNoContent());
        assertThat(commentRepository.existsById(first)).isFalse();

        Long second = comment(discussion, author).getId();
        mvc.perform(delete("/api/comments/" + second).header("Authorization", users.bearer(admin)))
                .andExpect(status().isNoContent());
        assertThat(commentRepository.existsById(second)).isFalse();
    }

    @Test
    void aStrangerCannotEditSomeoneElsesDiscussion() throws Exception {
        Long id = discussion(users.create("Usuario de prueba", User.Role.USER, false)).getId();

        mvc.perform(put("/api/discussions/" + id).header("Authorization", users.bearer(users.create("Usuario de prueba", User.Role.USER, false)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Título nuevo\",\"content\":\"Contenido nuevo largo\",\"category\":\"GENERAL\"}"))
                .andExpect(status().isForbidden());
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
