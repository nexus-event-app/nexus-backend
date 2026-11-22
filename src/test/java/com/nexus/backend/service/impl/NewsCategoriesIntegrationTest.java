package com.nexus.backend.service.impl;

import com.nexus.backend.entity.News;
import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.repository.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NewsCategoriesIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private NewsRepository newsRepository;

    @BeforeEach
    void seed() {
        newsRepository.deleteAll();
        news("Novedades de Python", NewsCategory.PROGRAMACION, "es", true);
        news("Nuevo modelo de IA", NewsCategory.IA, "es", true);
        news("Otro modelo de IA", NewsCategory.IA, "es", true);
        news("PostgreSQL 18 released", NewsCategory.DATOS, "en", true);
        news("Borrador de seguridad", NewsCategory.CIBERSEGURIDAD, "es", false);
    }

    @Test
    void listsOnlyTopicsWithPublishedNewsInFilterOrder() throws Exception {
        mvc.perform(get("/api/news/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", contains("IA", "PROGRAMACION", "DATOS")));
        mvc.perform(get("/api/news/categories").param("language", "es"))
                .andExpect(jsonPath("$", contains("IA", "PROGRAMACION")));
        mvc.perform(get("/api/news/categories").param("language", "en"))
                .andExpect(jsonPath("$", contains("DATOS")));
    }

    private void news(String title, NewsCategory category, String language, boolean active) {
        News news = new News();
        news.setTitle(title);
        news.setContent(title);
        news.setCategory(category);
        news.setLanguage(language);
        news.setIsActive(active);
        newsRepository.save(news);
    }
}
