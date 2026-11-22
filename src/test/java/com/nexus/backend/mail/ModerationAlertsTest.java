package com.nexus.backend.mail;

import com.nexus.backend.entity.Testimonial;
import com.nexus.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ModerationAlertsTest {

    private final UserRepository users = mock(UserRepository.class);
    private final EmailTemplates templates = mock(EmailTemplates.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);

    @Test
    void withAModerationEmailOnlyThatAddressIsNotified() {
        when(templates.newTestimonial(anyString(), any(), any(), any(), any()))
                .thenAnswer(call -> new EmailMessage(call.getArgument(0), "s", "t", "h"));

        new ModerationAlerts(users, templates, events, " proyecto@test.com ").newTestimonial(testimonial());

        verify(templates).newTestimonial(eq("proyecto@test.com"), eq("Marta"), any(), any(), any());
        verify(users, never()).findAdminEmails();
    }

    @Test
    void withoutItEveryAdministratorIsNotified() {
        when(users.findAdminEmails()).thenReturn(List.of("ana@test.com", "bea@test.com"));
        when(templates.newTestimonial(anyString(), any(), any(), any(), any()))
                .thenAnswer(call -> new EmailMessage(call.getArgument(0), "s", "t", "h"));

        new ModerationAlerts(users, templates, events, "").newTestimonial(testimonial());

        ArgumentCaptor<EmailRequestedEvent> sent = ArgumentCaptor.forClass(EmailRequestedEvent.class);
        verify(events, times(2)).publishEvent(sent.capture());
        assertThat(sent.getAllValues()).extracting(event -> event.message().to()).containsExactly("ana@test.com", "bea@test.com");
    }

    private static Testimonial testimonial() {
        Testimonial testimonial = new Testimonial();
        testimonial.setName("Marta");
        testimonial.setContent("Gracias a todas.");
        return testimonial;
    }
}
