package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateEventRequest;
import com.nexus.backend.dto.response.AdminEventResponse;
import com.nexus.backend.dto.response.EventResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.entity.CommunityEvent;
import com.nexus.backend.enums.EventMode;
import com.nexus.backend.enums.ReportAction;
import com.nexus.backend.enums.ReportStatus;
import com.nexus.backend.enums.ReportTargetType;
import com.nexus.backend.exception.BadRequestException;
import com.nexus.backend.exception.ConflictException;
import com.nexus.backend.exception.ForbiddenOperationException;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.mail.ModerationAlerts;
import com.nexus.backend.repository.CommunityEventRepository;
import com.nexus.backend.repository.ReportRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.AccountService;
import com.nexus.backend.service.EventService;
import com.nexus.backend.util.Pagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Eventos que comparte la comunidad. Se publican al momento (con límite diario y sin duplicados) y la moderación llega
 * por reportes y por el aviso por email. Los pasados dejan de verse solos y se borran a los {@value #KEEP_PAST_DAYS}
 * días.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    /** Para que nadie llene la lista: eventos que una persona puede compartir en 24 horas. */
    static final int DAILY_LIMIT = 5;
    static final int KEEP_PAST_DAYS = 90;

    private final CommunityEventRepository eventRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final ModerationAlerts moderationAlerts;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getUpcoming(Boolean forWomen, EventMode mode, int page, int size,
                                                   UserDetail viewer) {
        Long viewerId = viewer == null ? null : viewer.getId();
        return PageResponse.from(eventRepository
                .findUpcoming(today(), forWomen, mode, Pagination.unsorted(page, size))
                .map(event -> toResponse(event, viewerId)));
    }

    @Override
    @Transactional
    public EventResponse create(CreateEventRequest request, UserDetail actor) {
        accountService.assertCanParticipate(actor);
        String city = blankToNull(request.getCity());
        if (request.getMode() == EventMode.PRESENCIAL && city == null) {
            throw new BadRequestException("error.event.cityRequired");
        }
        String url = request.getUrl().trim();
        if (eventRepository.countByAuthorIdAndCreatedAtAfter(actor.getId(), LocalDateTime.now(clock).minusHours(24))
                >= DAILY_LIMIT) {
            throw new ConflictException("error.event.limit");
        }
        if (eventRepository.existsByUrlAndStartsOnAndActiveTrue(url, request.getDate())) {
            throw new ConflictException("error.event.duplicate");
        }

        CommunityEvent event = new CommunityEvent();
        event.setTitle(request.getTitle().trim());
        event.setDescription(request.getDescription().trim());
        event.setUrl(url);
        event.setStartsOn(request.getDate());
        event.setStartTime(request.getTime());
        event.setMode(request.getMode());
        event.setCity(request.getMode() == EventMode.ONLINE ? null : city);
        event.setForWomen(request.isForWomen());
        event.setAuthor(userRepository.getSessionUser(actor.getId()));
        CommunityEvent saved = eventRepository.save(event);
        moderationAlerts.newEvent(saved);
        log.info("Evento id={} compartido por usuario id={}", saved.getId(), actor.getId());
        return toResponse(saved, actor.getId());
    }

    @Override
    @Transactional
    public void delete(Long id, UserDetail actor) {
        CommunityEvent event = find(id);
        if (!actor.canManage(event.getAuthor().getId())) {
            throw new ForbiddenOperationException("error.event.deleteForbidden");
        }
        eventRepository.delete(event);
        // Sus reportes pendientes quedan resueltos: el evento ya no existe.
        reportRepository.closePending(ReportTargetType.EVENT, id, ReportStatus.RESOLVED, ReportAction.DELETE, null,
                LocalDateTime.now(clock));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminEventResponse> getAllForModeration(int page, int size) {
        LocalDate today = today();
        return PageResponse.from(eventRepository.findAllForModeration(Pagination.unsorted(page, size))
                .map(event -> new AdminEventResponse(event.getId(), event.getTitle(), event.getDescription(),
                        event.getUrl(), host(event.getUrl()), event.getStartsOn(), event.getStartTime(),
                        event.getMode(), event.getCity(), event.isForWomen(), event.isActive(),
                        event.getStartsOn().isBefore(today), event.getAuthor().getName(), event.getCreatedAt())));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isVisible(Long id) {
        return eventRepository.findById(id).filter(event -> event.isVisibleOn(today())).isPresent();
    }

    @Override
    @Transactional
    public void hide(Long id) {
        eventRepository.findById(id).ifPresent(event -> event.setActive(false));
    }

    /** Cada noche: fuera los eventos que pasaron hace más de {@value #KEEP_PAST_DAYS} días. */
    @Scheduled(cron = "${app.events.cleanup-cron:0 45 4 * * *}")
    @Transactional
    public void deleteOldEvents() {
        int deleted = eventRepository.deletePastBefore(today().minusDays(KEEP_PAST_DAYS));
        if (deleted > 0) {
            log.info("Borrados {} eventos pasados", deleted);
        }
    }

    private CommunityEvent find(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("error.event.notFound"));
    }

    private EventResponse toResponse(CommunityEvent event, Long viewerId) {
        return new EventResponse(event.getId(), event.getTitle(), event.getDescription(), event.getUrl(),
                host(event.getUrl()), event.getStartsOn(), event.getStartTime(), event.getMode(), event.getCity(),
                event.isForWomen(), viewerId != null && viewerId.equals(event.getAuthor().getId()));
    }

    /** «https://www.meetup.com/grupo/...» → «meetup.com». */
    static String host(String url) {
        try {
            String host = URI.create(url).getHost();
            return host == null ? null : host.toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
