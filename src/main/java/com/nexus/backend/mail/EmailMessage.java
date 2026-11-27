package com.nexus.backend.mail;

public record EmailMessage(String to, String subject, String text, String html, String replyTo) {

    public EmailMessage(String to, String subject, String text, String html) {
        this(to, subject, text, html, null);
    }

    @Override
    public String toString() {
        return "EmailMessage[to=" + to + ", subject=" + subject + "]";
    }
}
