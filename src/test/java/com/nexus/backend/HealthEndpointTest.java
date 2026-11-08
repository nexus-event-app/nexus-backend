package com.nexus.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El healthcheck del contenedor usa /actuator/health: debe responder UP aunque el correo no esté configurado
 * (como en desarrollo y en los tests), o Docker reiniciaría el backend sin motivo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthEndpointTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void healthIsUpWithoutMailServer() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
