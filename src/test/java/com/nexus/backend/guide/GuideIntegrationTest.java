package com.nexus.backend.guide;

import com.nexus.backend.entity.User;
import com.nexus.backend.support.TestUsers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GuideIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private TestUsers users;

    @Test
    void theFirstGuideIsPublishedAndAnyoneCanReadIt() throws Exception {
        mvc.perform(get("/api/guides"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].slug", hasItem("que-hacer-despues-de-un-bootcamp")))
                .andExpect(jsonPath("$[0].content").doesNotExist())
                .andExpect(jsonPath("$[0].readingMinutes").value(5));
        mvc.perform(get("/api/guides/que-hacer-despues-de-un-bootcamp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Qué hacer después de un bootcamp de programación: guía paso a paso"))
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.publishedAt").exists());
    }

    @Test
    void draftsAreOnlyVisibleInTheAdminPanelUntilTheyArePublished() throws Exception {
        String admin = users.bearer(users.create("Admin guías", User.Role.ADMIN, true));
        String body = """
                {"title":"Cómo preparar tu primera entrevista técnica","description":"Consejos para tu primera entrevista.",
                 "content":"## Antes de la entrevista\\n\\nRepasa los fundamentos.","published":false}
                """;
        String created = mvc.perform(post("/api/admin/guides").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("como-preparar-tu-primera-entrevista-tecnica"))
                .andExpect(jsonPath("$.publishedAt").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String id = created.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(get("/api/guides/como-preparar-tu-primera-entrevista-tecnica")).andExpect(status().isNotFound());
        mvc.perform(get("/api/guides")).andExpect(jsonPath("$[*].slug", not(hasItem("como-preparar-tu-primera-entrevista-tecnica"))));

        mvc.perform(put("/api/admin/guides/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body.replace("\"published\":false", "\"published\":true")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedAt").exists());
        mvc.perform(get("/api/guides/como-preparar-tu-primera-entrevista-tecnica")).andExpect(status().isOk());

        mvc.perform(post("/api/admin/guides").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());

        mvc.perform(delete("/api/admin/guides/" + id).header("Authorization", admin)).andExpect(status().isNoContent());
        mvc.perform(get("/api/guides/como-preparar-tu-primera-entrevista-tecnica")).andExpect(status().isNotFound());
    }

    @Test
    void onlyAdminsManageGuides() throws Exception {
        String user = users.bearer(users.create("Usuaria", User.Role.USER, true));
        mvc.perform(get("/api/admin/guides").header("Authorization", user)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/guides")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsAddressesThatAreNotValid() throws Exception {
        String admin = users.bearer(users.create("Admin guías", User.Role.ADMIN, true));
        mvc.perform(post("/api/admin/guides").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Guía\",\"slug\":\"Con Espacios\",\"description\":\"d\",\"content\":\"c\"}"))
                .andExpect(status().isBadRequest());
    }
}
