package com.nexus.backend.i18n;

import com.nexus.backend.entity.User;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Los errores salen en el idioma de la cabecera Accept-Language; sin ella, o con otro idioma, en castellano. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocalizedResponsesIntegrationTest {

    private static final long MISSING_ID = 987_654_321L;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void notFoundErrorsAreTranslated() throws Exception {
        mvc.perform(get("/api/news/" + MISSING_ID).header("Accept-Language", "en"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("News article not found"));
        mvc.perform(get("/api/news/" + MISSING_ID).header("Accept-Language", "ca"))
                .andExpect(jsonPath("$.message").value("Notícia no trobada"));
        mvc.perform(get("/api/news/" + MISSING_ID).header("Accept-Language", "es"))
                .andExpect(jsonPath("$.message").value("Noticia no encontrada"));
    }

    @Test
    void spanishIsTheDefaultAndUnsupportedLanguagesFallBackToIt() throws Exception {
        mvc.perform(get("/api/news/" + MISSING_ID))
                .andExpect(jsonPath("$.message").value("Noticia no encontrada"));
        mvc.perform(get("/api/news/" + MISSING_ID).header("Accept-Language", "fr-FR,fr;q=0.9"))
                .andExpect(jsonPath("$.message").value("Noticia no encontrada"));
    }

    @Test
    void securityFilterErrorsAreTranslated() throws Exception {
        mvc.perform(get("/api/auth/me").header("Accept-Language", "ca"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticació requerida"));
        mvc.perform(get("/api/auth/me").header("Accept-Language", "en"))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void validationMessagesAreTranslated() throws Exception {
        String admin = adminToken();
        String emptyTitle = "{\"title\":\"\",\"content\":\"Contenido\"}";

        mvc.perform(post("/api/admin/news").header("Authorization", admin).header("Accept-Language", "en")
                        .contentType(MediaType.APPLICATION_JSON).content(emptyTitle))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid data"))
                .andExpect(jsonPath("$.fieldErrors.title").value("The title is required"));
        mvc.perform(post("/api/admin/news").header("Authorization", admin).header("Accept-Language", "ca")
                        .contentType(MediaType.APPLICATION_JSON).content(emptyTitle))
                .andExpect(jsonPath("$.fieldErrors.title").value("El títol és obligatori"));
        mvc.perform(post("/api/admin/news").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(emptyTitle))
                .andExpect(jsonPath("$.fieldErrors.title").value("El título es obligatorio"));
    }

    private String adminToken() {
        User user = new User();
        user.setName("Usuario de prueba");
        user.setEmail("i18n-admin-" + UUID.randomUUID() + "@test.com");
        user.setPassword(passwordEncoder.encode("Clave12345"));
        user.setRole(User.Role.ADMIN);
        user.setEnabled(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        return "Bearer " + jwtUtil.generateToken(saved.getEmail(), saved.getRole().name(), saved.getId());
    }
}
