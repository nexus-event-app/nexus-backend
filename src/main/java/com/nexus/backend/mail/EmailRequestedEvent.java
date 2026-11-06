package com.nexus.backend.mail;

/** Pide enviar un email cuando la transacción en curso se confirme. */
public record EmailRequestedEvent(EmailMessage message) {
}
