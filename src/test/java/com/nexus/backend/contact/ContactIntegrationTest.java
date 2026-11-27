package com.nexus.backend.contact;

import com.nexus.backend.mail.EmailMessage;
import com.nexus.backend.support.CapturingMail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(CapturingMail.class)
@SpringBootTest(properties = "app.contact.email=contacto@primercommit.test")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContactIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private CapturingMail.Sender mail;

    @BeforeEach
    void clearMail() {
        mail.sent.clear();
    }

    @Test
    void anyoneCanWriteAndTheMessageReachesTheContactEmailReadyToAnswer() throws Exception {
        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Oscar","email":"oscar@test.com","reason":"HELP",
                         "message":"No me llega el correo de confirmación."}
                        """))
                .andExpect(status().isAccepted());

        assertThat(mail.sent).hasSize(1);
        EmailMessage sent = mail.sent.get(0);
        assertThat(sent.to()).isEqualTo("contacto@primercommit.test");
        assertThat(sent.replyTo()).isEqualTo("oscar@test.com");
        assertThat(sent.subject()).isEqualTo("Contacto · Dudas o problemas · Oscar");
        assertThat(sent.text()).contains("No me llega el correo de confirmación.");
    }

    @Test
    void robotsThatFillTheHiddenFieldAreIgnoredQuietly() throws Exception {
        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Robot","email":"bot@test.com","reason":"IDEAS","message":"Compra ya mis productos.",
                         "website":"https://spam.test"}
                        """))
                .andExpect(status().isAccepted());

        assertThat(mail.sent).isEmpty();
    }

    @Test
    void rejectsIncompleteMessages() throws Exception {
        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"A\",\"email\":\"no-es-email\",\"message\":\"Hola\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.reason").exists());

        assertThat(mail.sent).isEmpty();
    }
}
