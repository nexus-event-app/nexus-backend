package com.nexus.backend.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * En desarrollo (MAIL_ENABLED=false) no se envía nada: el email, con sus enlaces, se escribe en el log
 * para poder probar los flujos sin un servicio de correo.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(EmailMessage message) {
        log.info("""

                ===== EMAIL (no enviado: MAIL_ENABLED=false) =====
                Para: {}
                Asunto: {}

                {}
                ==================================================""", message.to(), message.subject(), message.text());
    }
}
