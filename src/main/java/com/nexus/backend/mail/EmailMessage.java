package com.nexus.backend.mail;

/** Email listo para enviar, con versión en texto plano y en HTML. */
public record EmailMessage(String to, String subject, String text, String html) {

    @Override
    public String toString() {
        return "EmailMessage[to=" + to + ", subject=" + subject + "]";
    }
}
