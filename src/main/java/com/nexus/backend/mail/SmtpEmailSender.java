package com.nexus.backend.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final String from;
    private final String replyTo;

    public SmtpEmailSender(JavaMailSender mailSender, @Value("${app.mail.from}") String from,
                           @Value("${app.contact.email:}") String replyTo) {
        this.mailSender = mailSender;
        this.from = from;
        this.replyTo = replyTo == null ? "" : replyTo.trim();
    }

    @Override
    public void send(EmailMessage message) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            String answerTo = message.replyTo() == null || message.replyTo().isBlank() ? replyTo : message.replyTo();
            if (!answerTo.isEmpty()) {
                helper.setReplyTo(answerTo);
            }
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.text(), message.html());
            mailSender.send(mime);
        } catch (MessagingException | MailException e) {
            throw new IllegalStateException("No se pudo enviar el email: " + e.getMessage(), e);
        }
    }
}
