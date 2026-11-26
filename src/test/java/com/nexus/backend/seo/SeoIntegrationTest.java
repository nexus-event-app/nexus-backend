package com.nexus.backend.seo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeoIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void anyoneCanReadTheSummaryForAiAssistantsWithTheRealGuides() throws Exception {
        mvc.perform(get("/llms.txt"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/markdown")))
                .andExpect(content().string(containsString("# Primer Commit")))
                .andExpect(content().string(containsString("/guias/sindrome-del-impostor")));
    }

    @Test
    void robotsPointsAiAssistantsToTheSummary() throws Exception {
        mvc.perform(get("/robots.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/llms.txt")));
    }
}
