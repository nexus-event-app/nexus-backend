package com.nexus.backend.mail;

import com.nexus.backend.entity.Testimonial;
import com.nexus.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Avisos por email de lo que espera moderación, para no tener que entrar al panel a mirar. Van a MODERATION_EMAIL
 * (el correo del proyecto) o, si no se configura, a las cuentas de administración. Se envían después de guardar, en
 * segundo plano (EmailDispatcher): si el correo falla, el testimonio se guarda igual.
 */
@Slf4j
@Component
public class ModerationAlerts {

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

    private List<String> recipients() {
        return moderationEmail.isEmpty() ? userRepository.findAdminEmails() : List.of(moderationEmail);
    }
}
