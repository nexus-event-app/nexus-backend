package com.nexus.backend.support;

import com.nexus.backend.mail.EmailSender;
import com.nexus.backend.mail.EmailMessage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@TestConfiguration
public class CapturingMail {

    @Bean
    @Primary
    Sender capturingEmailSender() {
        return new Sender();
    }

    public static class Sender implements EmailSender {

        public final List<EmailMessage> sent = new CopyOnWriteArrayList<>();

        @Override
        public void send(EmailMessage message) {
            sent.add(message);
        }
    }
}
