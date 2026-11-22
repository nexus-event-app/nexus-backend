package com.nexus.backend.mail;

import com.nexus.backend.entity.User;
import com.nexus.backend.support.CapturingMail;
import com.nexus.backend.support.TestUsers;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(CapturingMail.class)
@SpringBootTest(properties = {"app.moderation.email=moderacion@primercommit.test", "app.public-url=https://primercommit.test"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ModerationAlertsIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private CapturingMail.Sender mail;
    @Autowired
    private TestUsers users;

    @BeforeEach
    void clearMail() {
        mail.sent.clear();
    }

    @Test
    void aNewTestimonialSendsTheFullTextToTheModerationEmail() throws Exception {
        User author = users.create("Marta", User.Role.USER, true);
        mvc.perform(post("/api/testimonials").header("Authorization", users.bearer(author))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Marta","role":"Desarrolladora junior","location":"Valencia",
                                 "content":"Encontré mi primer trabajo gracias a la comunidad.\\nY aprendí <muchísimo>."}
                                """))
                .andExpect(status().isCreated());

        EmailMessage alert = awaitEmail();
        assertThat(alert.to()).isEqualTo("moderacion@primercommit.test");
        assertThat(alert.subject()).isEqualTo("Nuevo testimonio de Marta para revisar");
        assertThat(alert.text())
                .contains("Marta (Desarrolladora junior · Valencia)")
                .contains("Encontré mi primer trabajo gracias a la comunidad.")
                .contains("https://primercommit.test/admin/testimonios");
        assertThat(alert.html())
                .contains("Y aprend&iacute; &lt;much&iacute;simo&gt;.")
                .doesNotContain("<much")
                .contains("comunidad.<br>Y aprend")
                .contains("href=\"https://primercommit.test/admin/testimonios\"");
        assertThat(mail.sent).hasSize(1);
    }

    private EmailMessage awaitEmail() throws InterruptedException {
        for (int i = 0; i < 50 && mail.sent.isEmpty(); i++) {
            Thread.sleep(100);
        }
        assertThat(mail.sent).as("aviso de moderación enviado").isNotEmpty();
        return mail.sent.get(0);
    }
}
