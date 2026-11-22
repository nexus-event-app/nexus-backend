package com.nexus.backend.mail;

public record EmailMessage(String to, String subject, String text, String html) {

    @Override
    public String toString() {
        return "EmailMessage[to=" + to + ", subject=" + subject + "]";
    }
}
