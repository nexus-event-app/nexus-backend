package com.nexus.backend.learning;

import com.nexus.backend.entity.User;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Progreso en las rutas de aprendizaje: privado, idempotente y con identificadores validados. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LearningProgressIntegrationTest {

    private static final String BASE = "/api/learning/progress";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void requiresLogin() throws Exception {
        mvc.perform(get(BASE)).andExpect(status().isUnauthorized());
        mvc.perform(put(BASE + "/frontend/javascript")).andExpect(status().isUnauthorized());
    }

    @Test
    void savesAndUndoesStepsOnlyForTheirOwner() throws Exception {
        String ana = bearer(user("Ana"));
        String luis = bearer(user("Luis"));

        mvc.perform(put(BASE + "/frontend/fundamentos").header("Authorization", ana)).andExpect(status().isNoContent());
        mvc.perform(put(BASE + "/frontend/javascript").header("Authorization", ana)).andExpect(status().isNoContent());
        // Repetir la marca no la duplica.
        mvc.perform(put(BASE + "/frontend/javascript").header("Authorization", ana)).andExpect(status().isNoContent());

        mvc.perform(get(BASE).header("Authorization", ana))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].pathId").value("frontend"))
                .andExpect(jsonPath("$[0].stepId").value("fundamentos"))
                .andExpect(jsonPath("$[0].completedAt").exists());
        mvc.perform(get(BASE).header("Authorization", luis)).andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(delete(BASE + "/frontend/fundamentos").header("Authorization", ana)).andExpect(status().isNoContent());
        mvc.perform(delete(BASE + "/frontend/fundamentos").header("Authorization", ana)).andExpect(status().isNoContent());
        mvc.perform(get(BASE).header("Authorization", ana))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].stepId").value("javascript"));
    }

    @Test
    void rejectsInvalidIdentifiers() throws Exception {
        String ana = bearer(user("Ana"));
        for (String path : new String[]{"/Frontend/x", "/frontend/-malo", "/frontend/con_guion_bajo",
                "/frontend/" + "a".repeat(41), "/frontend/etapa-"}) {
            mvc.perform(put(BASE + path).header("Authorization", ana)).andExpect(status().isBadRequest());
        }
    }

    private User user(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail("learning-" + UUID.randomUUID() + "@test.com");
        user.setPassword(passwordEncoder.encode("Clave12345"));
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String bearer(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
    }
}
