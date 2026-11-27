package com.nexus.backend.mail;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SmtpEmailSenderTest {

    private final EmailMessage message = new EmailMessage("ana@test.com", "Asunto", "Texto", "<p>Texto</p>");

    @Test
    void repliesGoToTheContactAddressBecauseTheSenderHasNoInbox() throws Exception {
        MimeMessage sent = send("contacto@primercommit.test");

        assertThat(sent.getFrom()[0]).isEqualTo(new InternetAddress("Primer Commit <hola@primercommit.test>"));
        assertThat(sent.getReplyTo()[0]).isEqualTo(new InternetAddress("contacto@primercommit.test"));
    }

    @Test
    void withoutAContactAddressRepliesGoToTheSender() throws Exception {
        MimeMessage sent = send("");

        assertThat(sent.getReplyTo()[0]).isEqualTo(new InternetAddress("Primer Commit <hola@primercommit.test>"));
    }

    private MimeMessage send(String contact) {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        new SmtpEmailSender(mailSender, "Primer Commit <hola@primercommit.test>", contact).send(message);
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }
}
