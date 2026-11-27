package com.nexus.backend.contact;

import com.nexus.backend.dto.request.ContactRequest;
import com.nexus.backend.i18n.Messages;
import com.nexus.backend.mail.ModerationAlerts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ContactService {

    private static final Locale TEAM_LOCALE = Locale.forLanguageTag("es");

    private final ModerationAlerts alerts;
    private final Messages messages;
    private final String contactEmail;

    public ContactService(ModerationAlerts alerts, Messages messages, @Value("${app.contact.email:}") String contactEmail) {
        this.alerts = alerts;
        this.messages = messages;
        this.contactEmail = contactEmail;
    }

    public void send(ContactRequest request) {
        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            return;
        }
        String reason = messages.get(TEAM_LOCALE, "contact.reason." + request.getReason().name());
        alerts.contactMessage(contactEmail, request.getName(), request.getEmail(), reason, request.getMessage());
    }
}
