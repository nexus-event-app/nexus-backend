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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
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
                .andExpect(jsonPath("$[*].slug", hasItem("como-preparar-tu-primera-entrevista-tecnica")))
                .andExpect(jsonPath("$[?(@.slug == 'como-preparar-tu-primera-entrevista-tecnica')].category", hasItem("EMPLEO")))
                .andExpect(jsonPath("$[?(@.slug == 'que-hacer-despues-de-un-bootcamp')].category", hasItem("PRIMEROS_PASOS")))
                .andExpect(jsonPath("$[0].content").doesNotExist());
        mvc.perform(get("/api/guides/que-hacer-despues-de-un-bootcamp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Qué hacer después de un bootcamp de programación: guía paso a paso"))
                .andExpect(jsonPath("$.content").value(containsString("un foro para resolver dudas entre todas y todos")))
                .andExpect(jsonPath("$.content").value(not(containsString("tonta"))))
                .andExpect(jsonPath("$.content").value(containsString("(/guias/como-preparar-tu-primera-entrevista-tecnica)")))
                .andExpect(jsonPath("$.publishedAt").exists());
    }

    @Test
    void eachPersonReadsTheGuidesInTheirLanguage() throws Exception {
        mvc.perform(get("/api/guides/que-hacer-despues-de-un-bootcamp").header("Accept-Language", "ca"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("ca"))
                .andExpect(jsonPath("$.title").value("Què fer després d’un bootcamp de programació: guia pas a pas"))
                .andExpect(jsonPath("$.languages", contains("es", "ca", "en")));
        mvc.perform(get("/api/guides/git-y-github-para-principiantes").header("Accept-Language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.title").value("Git and GitHub for beginners: your first commit step by step"))
                .andExpect(jsonPath("$.category").value("HERRAMIENTAS"));
        mvc.perform(get("/api/guides").header("Accept-Language", "en"))
                .andExpect(jsonPath("$[?(@.slug == 'tu-primer-meetup-tech')].title",
                        hasItem("Your first tech meetup: what to expect and how to make the most of it")))
                .andExpect(jsonPath("$[?(@.slug == 'tu-primer-meetup-tech')].category", hasItem("COMUNIDAD")));
    }

    @Test
    void eachGuideShowsItsKeywordsInTheReaderLanguage() throws Exception {
        mvc.perform(get("/api/guides/sindrome-del-impostor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keywords[0]").value("Síndrome del impostor"))
                .andExpect(jsonPath("$.keywords.length()").value(10))
                .andExpect(jsonPath("$.content").value(not(containsString("tranquila"))))
                .andExpect(jsonPath("$.content").value(not(containsString("sola"))))
                .andExpect(jsonPath("$.content").value(containsString("hay más gente como tú")));
        mvc.perform(get("/api/guides/sindrome-del-impostor").header("Accept-Language", "ca"))
                .andExpect(jsonPath("$.content").value(not(containsString("sola"))))
                .andExpect(jsonPath("$.content").value(not(containsString("mateixa"))))
                .andExpect(jsonPath("$.content").value(containsString("hi ha més gent com tu")));
        mvc.perform(get("/api/guides/git-y-github-para-principiantes").header("Accept-Language", "en"))
                .andExpect(jsonPath("$.keywords", hasItem("Pull request")))
                .andExpect(jsonPath("$.keywords[0]").value("Git and GitHub"));
        mvc.perform(get("/api/guides"))
                .andExpect(jsonPath("$[0].keywords").doesNotExist());
    }

    @Test
    void theAdminPanelSavesTheKeywordsOfEachLanguage() throws Exception {
        String admin = users.bearer(users.create("Admin guías", User.Role.ADMIN, true));
        mvc.perform(post("/api/admin/guides").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Guía con etiquetas","description":"Una guía.","content":"## Hola\\n\\nTexto.",
                                 "keywords":" Uno ,  Dos ,, Tres ","keywordsCa":"U, Dos","category":"EMPLEO","published":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.keywordsCa").value("U, Dos"));
        mvc.perform(get("/api/guides/guia-con-etiquetas"))
                .andExpect(jsonPath("$.keywords", contains("Uno", "Dos", "Tres")));
        mvc.perform(get("/api/guides/guia-con-etiquetas").header("Accept-Language", "ca"))
                .andExpect(jsonPath("$.language").value("es"))
                .andExpect(jsonPath("$.keywords", contains("Uno", "Dos", "Tres")));
    }

    @Test
    void aGuideWithoutTranslationIsShownInSpanish() throws Exception {
        String admin = users.bearer(users.create("Admin guías", User.Role.ADMIN, true));
        mvc.perform(post("/api/admin/guides").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Solo en castellano","description":"Una guía sin traducir.",
                                 "content":"## Hola\\n\\nTexto.","titleCa":"Només el títol","category":"EMPLEO","published":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titleCa").value("Només el títol"))
                .andExpect(jsonPath("$.languages", contains("es")));
        mvc.perform(get("/api/guides/solo-en-castellano").header("Accept-Language", "ca"))
                .andExpect(jsonPath("$.language").value("es"))
                .andExpect(jsonPath("$.title").value("Solo en castellano"));
    }

    @Test
    void theInterviewGuideUsesRespectfulWording() throws Exception {
        mvc.perform(get("/api/guides/como-preparar-tu-primera-entrevista-tecnica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(not(containsString("chino"))))
                .andExpect(jsonPath("$.content").value(containsString("Si alguna palabra no la conoces")));
    }

    @Test
    void draftsAreOnlyVisibleInTheAdminPanelUntilTheyArePublished() throws Exception {
        String admin = users.bearer(users.create("Admin guías", User.Role.ADMIN, true));
        String body = """
                {"title":"Cómo escribir un buen README","description":"Consejos para tu README.",
                 "content":"## Antes de la entrevista\\n\\nRepasa los fundamentos.","category":"HERRAMIENTAS","published":false}
                """;
        String created = mvc.perform(post("/api/admin/guides").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("como-escribir-un-buen-readme"))
                .andExpect(jsonPath("$.category").value("HERRAMIENTAS"))
                .andExpect(jsonPath("$.publishedAt").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String id = created.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(get("/api/guides/como-escribir-un-buen-readme")).andExpect(status().isNotFound());
        mvc.perform(get("/api/guides")).andExpect(jsonPath("$[*].slug", not(hasItem("como-escribir-un-buen-readme"))));

        mvc.perform(put("/api/admin/guides/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body.replace("\"published\":false", "\"published\":true")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedAt").exists());
        mvc.perform(get("/api/guides/como-escribir-un-buen-readme")).andExpect(status().isOk());

        mvc.perform(post("/api/admin/guides").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());

        mvc.perform(delete("/api/admin/guides/" + id).header("Authorization", admin)).andExpect(status().isNoContent());
        mvc.perform(get("/api/guides/como-escribir-un-buen-readme")).andExpect(status().isNotFound());
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
