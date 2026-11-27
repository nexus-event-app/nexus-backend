package com.nexus.backend.mail;

import com.nexus.backend.entity.CommunityEvent;
import com.nexus.backend.entity.Testimonial;
import com.nexus.backend.enums.EventMode;
import com.nexus.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class ModerationAlerts {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"));

    private final UserRepository userRepository;
    private final EmailTemplates emailTemplates;
    private final ApplicationEventPublisher events;
    private final String moderationEmail;

    public ModerationAlerts(UserRepository userRepository, EmailTemplates emailTemplates,
                            ApplicationEventPublisher events, @Value("${app.moderation.email:}") String moderationEmail) {
        this.userRepository = userRepository;
        this.emailTemplates = emailTemplates;
        this.events = events;
        this.moderationEmail = moderationEmail == null ? "" : moderationEmail.trim();
    }

    public void newTestimonial(Testimonial testimonial) {
        List<String> recipients = recipients();
        recipients.forEach(to -> events.publishEvent(new EmailRequestedEvent(emailTemplates.newTestimonial(
                to, testimonial.getName(), testimonial.getRole(), testimonial.getLocation(), testimonial.getContent()))));
        log.info("Aviso de testimonio nuevo id={} para {} destinatario(s)", testimonial.getId(), recipients.size());
    }

    public void newEvent(CommunityEvent event) {
        String when = event.getStartsOn().format(DATE) + (event.getStartTime() == null ? "" : ", " + event.getStartTime());
        String where = event.getMode() == EventMode.ONLINE ? "Online" : event.getCity();
        List<String> recipients = recipients();
        recipients.forEach(to -> events.publishEvent(new EmailRequestedEvent(emailTemplates.newEvent(to,
                event.getTitle(), when, where, event.isForWomen(), event.getDescription(), event.getUrl()))));
        log.info("Aviso de evento nuevo id={} para {} destinatario(s)", event.getId(), recipients.size());
    }

    public void heldForReview(String title, String content, String reason) {
        List<String> recipients = recipients();
        recipients.forEach(to -> events.publishEvent(new EmailRequestedEvent(
                emailTemplates.heldForReview(to, title, content, reason))));
        log.info("Aviso de contenido retenido para {} destinatario(s)", recipients.size());
    }

    public void contactMessage(String contactEmail, String name, String email, String reason, String message) {
        List<String> recipients = contactEmail == null || contactEmail.isBlank() ? recipients() : List.of(contactEmail.trim());
        recipients.forEach(to -> events.publishEvent(new EmailRequestedEvent(
                emailTemplates.contactMessage(to, name, email, reason, message))));
        log.info("Mensaje de contacto ({}) para {} destinatario(s)", reason, recipients.size());
    }

    private List<String> recipients() {
        return moderationEmail.isEmpty() ? userRepository.findAdminEmails() : List.of(moderationEmail);
    }
}
