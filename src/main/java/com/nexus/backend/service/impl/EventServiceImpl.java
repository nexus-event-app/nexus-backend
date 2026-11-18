package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.EventRequest;
import com.nexus.backend.dto.response.AdminEventResponse;
import com.nexus.backend.dto.response.EventDayResponse;
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
import com.nexus.backend.repository.spec.EventSpecifications;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

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
    /** El calendario pide un mes (con los días que asoman de los meses de al lado): unas seis semanas. */
    static final int MAX_CALENDAR_DAYS = 62;

    private final CommunityEventRepository eventRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final ModerationAlerts moderationAlerts;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getUpcoming(Filter filter, int page, int size, UserDetail viewer) {
        Long viewerId = viewer == null ? null : viewer.getId();
        var spec = EventSpecifications.upcoming(today())
                .and(EventSpecifications.forWomen(filter.forWomen()))
                .and(EventSpecifications.mode(filter.mode()))
                .and(EventSpecifications.inCity(filter.city()))
                .and(EventSpecifications.onDay(filter.day()));
        return PageResponse.from(eventRepository.findAll(spec, Pagination.soonestFirst(page, size))
                .map(event -> toResponse(event, viewerId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDayResponse> getCalendar(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > MAX_CALENDAR_DAYS) {
            throw new BadRequestException("error.event.rangeTooLong");
        }
        // Los días pasados no salen: sus eventos ya no se ven en la agenda.
        LocalDate start = from.isBefore(today()) ? today() : from;
        if (to.isBefore(start)) {
            return List.of();
        }
        Map<LocalDate, EventDayResponse> days = new TreeMap<>();
        for (CommunityEvent event : eventRepository.findByActiveTrueAndStartsOnBetween(start, to)) {
            days.merge(event.getStartsOn(), new EventDayResponse(event.getStartsOn(), 1, event.isForWomen()),
                    (a, b) -> new EventDayResponse(a.date(), a.count() + 1, a.forWomen() || b.forWomen()));
        }
        return new ArrayList<>(days.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCities() {
        // Una sola entrada por ciudad aunque se haya escrito distinto («madrid», «Madrid »): la primera forma vista.
        Map<String, String> cities = new LinkedHashMap<>();
        eventRepository.findUpcomingCities(today(), EventMode.PRESENCIAL).stream()
                .filter(city -> !city.isBlank())
                .map(String::trim)
                .forEach(city -> cities.putIfAbsent(city.toLowerCase(Locale.ROOT), city));
        return cities.values().stream().sorted(Comparator.comparing(city -> city.toLowerCase(Locale.ROOT))).toList();
    }

    @Override
    @Transactional
    public EventResponse create(EventRequest request, UserDetail actor) {
        accountService.assertCanParticipate(actor);
        if (request.getDate().isBefore(today())) {
            throw new BadRequestException("validation.event.date.future");
        }
        assertCityWhenInPerson(request);
        if (eventRepository.countByAuthorIdAndCreatedAtAfter(actor.getId(), LocalDateTime.now(clock).minusHours(24))
                >= DAILY_LIMIT) {
            throw new ConflictException("error.event.limit");
        }
        if (eventRepository.existsByUrlAndStartsOnAndActiveTrue(request.getUrl().trim(), request.getDate())) {
            throw new ConflictException("error.event.duplicate");
        }

        CommunityEvent event = new CommunityEvent();
        apply(event, request);
        event.setAuthor(userRepository.getSessionUser(actor.getId()));
        CommunityEvent saved = eventRepository.save(event);
        moderationAlerts.newEvent(saved);
        log.info("Evento id={} compartido por usuario id={}", saved.getId(), actor.getId());
        return toResponse(saved, actor.getId());
    }

    @Override
    @Transactional
    public AdminEventResponse update(Long id, EventRequest request) {
        assertCityWhenInPerson(request);
        CommunityEvent event = find(id);
        apply(event, request);
        log.info("Evento id={} corregido desde el panel", id);
        return toAdminResponse(eventRepository.save(event), today());
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
                .map(event -> toAdminResponse(event, today)));
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

    private static void assertCityWhenInPerson(EventRequest request) {
        if (request.getMode() == EventMode.PRESENCIAL && blankToNull(request.getCity()) == null) {
            throw new BadRequestException("error.event.cityRequired");
        }
    }

    /** Lo mismo al compartir y al corregir: textos sin espacios de más; un evento online no lleva ciudad ni dirección. */
    private static void apply(CommunityEvent event, EventRequest request) {
        boolean online = request.getMode() == EventMode.ONLINE;
        event.setTitle(request.getTitle().trim());
        event.setDescription(request.getDescription().trim());
        event.setUrl(request.getUrl().trim());
        event.setStartsOn(request.getDate());
        event.setStartTime(request.getTime());
        event.setMode(request.getMode());
        event.setCity(online ? null : blankToNull(request.getCity()));
        event.setAddress(online ? null : blankToNull(request.getAddress()));
        event.setForWomen(request.isForWomen());
    }

    private EventResponse toResponse(CommunityEvent event, Long viewerId) {
        return new EventResponse(event.getId(), event.getTitle(), event.getDescription(), event.getUrl(),
                host(event.getUrl()), event.getStartsOn(), event.getStartTime(), event.getMode(), event.getCity(),
                event.getAddress(), event.isForWomen(), viewerId != null && viewerId.equals(event.getAuthor().getId()));
    }

    private static AdminEventResponse toAdminResponse(CommunityEvent event, LocalDate today) {
        return new AdminEventResponse(event.getId(), event.getTitle(), event.getDescription(), event.getUrl(),
                host(event.getUrl()), event.getStartsOn(), event.getStartTime(), event.getMode(), event.getCity(),
                event.getAddress(), event.isForWomen(), event.isActive(), event.getStartsOn().isBefore(today),
                event.getAuthor().getName(), event.getCreatedAt());
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
