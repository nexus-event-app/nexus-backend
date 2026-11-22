package com.nexus.backend.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    private final boolean showContent;

    public LoggingEmailSender(Environment environment) {
        this.showContent = environment.acceptsProfiles(Profiles.of("dev", "test"));
    }

    @Override
    public void send(EmailMessage message) {
        if (!showContent) {
            log.warn("Email «{}» no enviado: el correo está desactivado (MAIL_ENABLED=false).", message.subject());
            return;
        }
        log.info("""

                ===== EMAIL (no enviado: MAIL_ENABLED=false) =====
                Para: {}
                Asunto: {}

                {}
                ==================================================""", message.to(), message.subject(), message.text());
    }
}
