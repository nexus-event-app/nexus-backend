package com.nexus.backend.performance;

import com.nexus.backend.entity.Comment;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.Testimonial;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.repository.CommentRepository;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.entity.User;
import com.nexus.backend.repository.TestimonialRepository;
import com.nexus.backend.repository.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Las listas no deben hacer una consulta más por cada elemento (el problema «N+1»): con muchos testimonios, cargar
 * a sus autores uno a uno haría la página cada vez más lenta.
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ListQueriesIntegrationTest {

    private static final int ITEMS = 6;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestimonialRepository testimonialRepository;
    @Autowired
    private DiscussionRepository discussionRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Discussion lastDiscussion;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        for (int i = 0; i < ITEMS; i++) {
            User author = new User();
            author.setEmail("t-" + UUID.randomUUID() + "@example.com");
            author.setName("Autora " + i);
            author.setPassword("x");
            author = userRepository.save(author);

            Testimonial testimonial = new Testimonial();
            testimonial.setName("Autora " + i);
            testimonial.setContent("Un testimonio de prueba suficientemente largo para ser válido número " + i);
            testimonial.setUser(author);
            testimonial.setIsPending(false);
            testimonial.setIsActive(true);
            testimonial.setIsHighlighted(false);
            testimonialRepository.save(testimonial);

            Discussion discussion = new Discussion();
            discussion.setTitle("Discusión de prueba número " + i);
            discussion.setContent("Contenido de la discusión de prueba número " + i);
            discussion.setCategory(DiscussionCategory.GENERAL);
            discussion.setAuthor(author);
            lastDiscussion = discussionRepository.save(discussion);
        }
        for (User author : userRepository.findAll().subList(0, ITEMS)) {
            Comment comment = new Comment();
            comment.setContent("Una respuesta de prueba");
            comment.setDiscussion(lastDiscussion);
            comment.setAuthor(author);
            commentRepository.save(comment);
        }
    }

    @Test
    void publishedTestimonialsLoadTheirAuthorsInTheSameQuery() throws Exception {
        statistics.clear();
        mvc.perform(get("/api/testimonials")).andExpect(status().isOk());
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(2);
    }

    @Test
    void otherPublicListsKeepAConstantNumberOfQueries() throws Exception {
        for (String path : new String[]{"/api/discussions", "/api/comments/discussion/" + lastDiscussion.getId(),
                "/api/news", "/api/jobs"}) {
            statistics.clear();
            mvc.perform(get(path)).andExpect(status().isOk());
            assertThat(statistics.getPrepareStatementCount()).as(path).isLessThanOrEqualTo(4);
        }
    }
}
