package com.nexus.backend.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Reglas de seguridad básicas de la API, de extremo a extremo con la configuración real. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void publicRegistrationAlwaysCreatesUserRole() throws Exception {
        String body = """
                {"name":"Intruso","email":"%s","password":"Clave12345","role":"ADMIN"}
                """.formatted(uniqueEmail());

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    void adminEndpointsRequireAdminRole() throws Exception {
        String userToken = register();

        mvc.perform(get("/api/admin/import-sources")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
        mvc.perform(get("/api/admin/import-sources").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
        mvc.perform(post("/api/admin/news").header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"x\",\"content\":\"y\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/admin/import-sources").header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void identityComesOnlyFromTheToken() throws Exception {
        mvc.perform(delete("/api/discussions/1").header("User-Id", "1")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/discussions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Título válido\",\"content\":\"Contenido suficientemente largo\",\"category\":\"GENERAL\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenOnPublicEndpointIsTreatedAsAnonymous() throws Exception {
        mvc.perform(get("/api/news").header("Authorization", "Bearer token-falso")).andExpect(status().isOk());
    }

    @Test
    void disabledUserTokensStopWorking() throws Exception {
        String email = uniqueEmail();
        String token = register(email);
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setEnabled(false);
        userRepository.save(user);

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }

    private String register() throws Exception {
        return register(uniqueEmail());
    }

    private String register(String email) throws Exception {
        String body = "{\"name\":\"Usuaria\",\"email\":\"" + email + "\",\"password\":\"Clave12345\"}";
        String response = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return token(response);
    }

    private String adminToken() throws Exception {
        String email = "admin-" + UUID.randomUUID() + "@test.com";
        User admin = new User();
        admin.setName("Admin");
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode("Admin12345"));
        admin.setRole(User.Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Admin12345\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return token(response);
    }

    private String token(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        String token = node.path("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    private static String uniqueEmail() {
        return "u-" + UUID.randomUUID() + "@test.com";
    }
}
