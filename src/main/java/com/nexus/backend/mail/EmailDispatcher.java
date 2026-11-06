package com.nexus.backend.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envía los emails en segundo plano y solo después de confirmar la transacción, para no mandar un enlace
 * cuyo token no llegó a guardarse ni hacer esperar al usuario mientras responde el servidor de correo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailDispatcher {

    private final EmailSender emailSender;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEmailRequested(EmailRequestedEvent event) {
        try {
            emailSender.send(event.message());
        } catch (RuntimeException e) {
            log.error("Fallo al enviar el email '{}': {}", event.message().subject(), e.getMessage());
        }
    }
}
