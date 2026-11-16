package com.nexus.backend.mail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/** Sin servicio de correo, el email solo se escribe entero en el log en desarrollo: fuera, sus enlaces son secretos. */
@ExtendWith(OutputCaptureExtension.class)
class LoggingEmailSenderTest {

    private static final EmailMessage MESSAGE = new EmailMessage("persona@example.com", "Restablece tu contraseña",
            "Entra en https://web.test/restablecer?token=secreto-de-un-solo-uso", "<p>html</p>");

    @Test
    void inDevelopmentTheWholeEmailIsLoggedToTestTheFlows(CapturedOutput output) {
        new LoggingEmailSender(environment("dev")).send(MESSAGE);
        assertThat(output).contains("persona@example.com", "token=secreto-de-un-solo-uso");
    }

    @Test
    void outsideDevelopmentNeitherTheRecipientNorTheLinksAreLogged(CapturedOutput output) {
        new LoggingEmailSender(environment("prod")).send(MESSAGE);
        assertThat(output).contains("MAIL_ENABLED").doesNotContain("persona@example.com", "secreto-de-un-solo-uso");
    }

    private static MockEnvironment environment(String profile) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profile);
        return environment;
    }
}
