package com.nexus.backend.search;

import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.JobOffer;
import com.nexus.backend.entity.News;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.WorkMode;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.JobOfferRepository;
import com.nexus.backend.repository.NewsRepository;
import com.nexus.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Búsqueda global: solo contenido público, agrupado por sección, sin datos privados. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SearchIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private JobOfferRepository jobOfferRepository;
    @Autowired
    private DiscussionRepository discussionRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findsOnlyPublicContentInEverySection() throws Exception {
        String word = "zq" + UUID.randomUUID().toString().substring(0, 6);
        news("Novedades de " + word, true);
        news("Borrador de " + word, false);
        job("Junior " + word, true, null);
        job("Caducada " + word, true, LocalDateTime.now().minusDays(1));
        job("Pausada " + word, false, null);
        User author = user("Ana " + word, "Frontend con " + word, true);
        user("Desactivada " + word, null, false);
        discussion(author, "Duda sobre " + word, true);
        discussion(author, "Oculta sobre " + word, false);

        mvc.perform(get("/api/search").param("q", word.toUpperCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.news.total").value(1))
                .andExpect(jsonPath("$.news.items[0].title").value("Novedades de " + word))
                .andExpect(jsonPath("$.jobs.total").value(1))
                .andExpect(jsonPath("$.jobs.items[0].title").value("Junior " + word))
                .andExpect(jsonPath("$.discussions.total").value(1))
                .andExpect(jsonPath("$.discussions.items[0].title").value("Duda sobre " + word))
                .andExpect(jsonPath("$.people.total").value(1))
                .andExpect(jsonPath("$.people.items[0].name").value("Ana " + word))
                .andExpect(jsonPath("$.people.items[0].email").doesNotExist());
    }

    @Test
    void neverMatchesPrivateDataSuchAsTheEmail() throws Exception {
        String word = "mail" + UUID.randomUUID().toString().substring(0, 6);
        User user = user("Persona", null, true);
        user.setEmail(word + "@test.com");
        userRepository.save(user);

        mvc.perform(get("/api/search").param("q", word))
                .andExpect(jsonPath("$.people.total").value(0));
    }

    @Test
    void ignoresQueriesThatAreTooShortAndCapsTheLimit() throws Exception {
        mvc.perform(get("/api/search").param("q", " a "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.news.total").value(0))
                .andExpect(jsonPath("$.people.items", hasSize(0)));

        String word = "lim" + UUID.randomUUID().toString().substring(0, 6);
        for (int i = 0; i < 12; i++) {
            news(word + " " + i, true);
        }
        mvc.perform(get("/api/search").param("q", word).param("limit", "50"))
                .andExpect(jsonPath("$.news.total").value(12))
                .andExpect(jsonPath("$.news.items", hasSize(10)))
                .andExpect(jsonPath("$.news.items[*].title", everyItem(startsWith(word))));
        mvc.perform(get("/api/search").param("q", word))
                .andExpect(jsonPath("$.news.items", hasSize(5)));
    }

    @Test
    void treatsWildcardsAsPlainText() throws Exception {
        news("Cien por cien", true);
        mvc.perform(get("/api/search").param("q", "%%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.news.total").value(0));
    }

    private void news(String title, boolean active) {
        News news = new News();
        news.setTitle(title);
        news.setContent("Contenido de la noticia de prueba");
        news.setIsActive(active);
        newsRepository.save(news);
    }

    private void job(String title, boolean active, LocalDateTime expiresAt) {
        JobOffer job = new JobOffer();
        job.setTitle(title);
        job.setCompany("Empresa");
        job.setDescription("Descripción de la oferta");
        job.setLocation("Barcelona");
        job.setWorkMode(WorkMode.REMOTE);
        job.setContractType(ContractType.FULL_TIME);
        job.setLevel(JobLevel.JUNIOR);
        job.setApplyUrl("https://example.com/oferta");
        job.setIsActive(active);
        job.setExpiresAt(expiresAt);
        job.setPublishedAt(LocalDateTime.now());
        jobOfferRepository.save(job);
    }

    private User user(String name, String headline, boolean enabled) {
        User user = new User();
        user.setName(name);
        user.setHeadline(headline);
        user.setEmail("search-" + UUID.randomUUID() + "@test.com");
        user.setPassword("x");
        user.setRole(User.Role.USER);
        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    private void discussion(User author, String title, boolean active) {
        Discussion discussion = new Discussion();
        discussion.setTitle(title);
        discussion.setContent("Contenido de la discusión de prueba");
        discussion.setCategory(DiscussionCategory.GENERAL);
        discussion.setAuthor(author);
        discussion.setIsActive(active);
        discussionRepository.save(discussion);
    }
}
