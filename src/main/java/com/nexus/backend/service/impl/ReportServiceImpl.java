package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateReportRequest;
import com.nexus.backend.dto.request.ResolveReportRequest;
import com.nexus.backend.dto.response.ReportCaseResponse;
import com.nexus.backend.dto.response.ReportCaseResponse.Target;
import com.nexus.backend.entity.Comment;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.Report;
import com.nexus.backend.entity.Testimonial;
import com.nexus.backend.enums.ReportAction;
import com.nexus.backend.enums.ReportReason;
import com.nexus.backend.enums.ReportStatus;
import com.nexus.backend.enums.ReportTargetType;
import com.nexus.backend.exception.BadRequestException;
import com.nexus.backend.exception.ConflictException;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.repository.CommentRepository;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.ReportRepository;
import com.nexus.backend.repository.TestimonialRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.AccountService;
import com.nexus.backend.service.CommentService;
import com.nexus.backend.service.DiscussionService;
import com.nexus.backend.service.ReportService;
import com.nexus.backend.service.TestimonialService;
import com.nexus.backend.util.SlugUtil;
import com.nexus.backend.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final Set<ReportStatus> PENDING = Set.of(ReportStatus.PENDING);
    private static final Set<ReportStatus> CLOSED = Set.of(ReportStatus.RESOLVED, ReportStatus.DISMISSED);
    /** La cola muestra como mucho estos contenidos a la vez: si hubiera más, se verían al ir resolviendo. */
    private static final int MAX_CASES = 100;
    private static final int EXCERPT_LENGTH = 280;

    private final AccountService accountService;
    private final ReportRepository reportRepository;
    private final DiscussionRepository discussionRepository;
    private final CommentRepository commentRepository;
    private final TestimonialRepository testimonialRepository;
    private final UserRepository userRepository;
    private final DiscussionService discussionService;
    private final CommentService commentService;
    private final TestimonialService testimonialService;

    /** Contenido reportable que se puede ver: quién lo escribió (null si fue un testimonio sin cuenta). */
    private record Owner(Long userId) {
    }

    @Override
    @Transactional
    public void create(CreateReportRequest request, UserDetail actor) {
        accountService.assertCanParticipate(actor);
        Owner owner = findVisibleOwner(request.targetType(), request.targetId())
                .orElseThrow(() -> new ResourceNotFoundException("error.report.targetNotFound"));
        if (Objects.equals(owner.userId(), actor.getId())) {
            throw new BadRequestException("error.report.ownContent");
        }
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(actor.getId(), request.targetType(),
                request.targetId())) {
            throw new ConflictException("error.report.duplicate");
        }
        Report report = new Report();
        report.setReporter(userRepository.getReferenceById(actor.getId()));
        report.setTargetType(request.targetType());
        report.setTargetId(request.targetId());
        report.setReason(request.reason());
        report.setDetails(request.details());
        reportRepository.save(report);
        log.info("Reporte de {} id={} por usuario id={} ({})", request.targetType(), request.targetId(), actor.getId(),
                request.reason());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportCaseResponse> getCases(boolean pending) {
        Set<ReportStatus> statuses = pending ? PENDING : CLOSED;
        return reportRepository.findReportedTargets(statuses, PageRequest.of(0, MAX_CASES)).stream()
                .map(row -> toCase(row, statuses))
                .toList();
    }

    @Override
    @Transactional
    public void resolve(ResolveReportRequest request, UserDetail actor) {
        ReportTargetType type = request.targetType();
        Long id = request.targetId();
        if (reportRepository.findByTarget(type, id, PENDING).isEmpty()) {
            throw new ResourceNotFoundException("error.report.notFound");
        }
        boolean exists = targetExists(type, id);
        switch (request.action()) {
            case DISMISS -> close(type, id, ReportStatus.DISMISSED, ReportAction.DISMISS, actor);
            case HIDE -> {
                hide(type, id);
                close(type, id, ReportStatus.RESOLVED, ReportAction.HIDE, actor);
            }
            case DELETE -> {
                // Al borrar el contenido, su servicio cierra también sus reportes pendientes.
                if (exists) {
                    delete(type, id, actor);
                }
                close(type, id, ReportStatus.RESOLVED, ReportAction.DELETE, actor);
            }
        }
        log.info("Reportes de {} id={} cerrados por usuario id={}: {}", type, id, actor.getId(), request.action());
    }

    private void close(ReportTargetType type, Long id, ReportStatus status, ReportAction action, UserDetail actor) {
        reportRepository.closePending(type, id, status, action, userRepository.getReferenceById(actor.getId()),
                LocalDateTime.now());
    }

    private void hide(ReportTargetType type, Long id) {
        switch (type) {
            case DISCUSSION -> discussionRepository.findById(id).ifPresent(discussion -> discussion.setIsActive(false));
            case TESTIMONIAL -> testimonialRepository.findById(id).ifPresent(testimonial -> testimonial.setIsActive(false));
            // Los comentarios no se pueden ocultar, solo borrar.
            case COMMENT -> throw new BadRequestException("error.report.cannotHideComment");
        }
    }

    private void delete(ReportTargetType type, Long id, UserDetail actor) {
        switch (type) {
            case DISCUSSION -> discussionService.deleteDiscussion(id, actor);
            case COMMENT -> commentService.deleteComment(id, actor);
            case TESTIMONIAL -> testimonialService.deleteTestimonial(id);
        }
    }

    private boolean targetExists(ReportTargetType type, Long id) {
        return switch (type) {
            case DISCUSSION -> discussionRepository.existsById(id);
            case COMMENT -> commentRepository.existsById(id);
            case TESTIMONIAL -> testimonialRepository.existsById(id);
        };
    }

    /** Autor del contenido si está publicado (lo oculto o pendiente de moderar no se puede reportar). */
    private Optional<Owner> findVisibleOwner(ReportTargetType type, Long id) {
        return switch (type) {
            case DISCUSSION -> discussionRepository.findById(id)
                    .filter(discussion -> Boolean.TRUE.equals(discussion.getIsActive()))
                    .map(discussion -> new Owner(discussion.getAuthor().getId()));
            case COMMENT -> commentRepository.findById(id)
                    .filter(comment -> Boolean.TRUE.equals(comment.getDiscussion().getIsActive()))
                    .map(comment -> new Owner(comment.getAuthor().getId()));
            case TESTIMONIAL -> testimonialRepository.findById(id)
                    .filter(ReportServiceImpl::isPublished)
                    .map(testimonial -> new Owner(testimonial.getUser() == null ? null : testimonial.getUser().getId()));
        };
    }

    private static boolean isPublished(Testimonial testimonial) {
        return Boolean.TRUE.equals(testimonial.getIsActive()) && !Boolean.TRUE.equals(testimonial.getIsPending());
    }

    private ReportCaseResponse toCase(ReportRepository.ReportedTarget row, Set<ReportStatus> statuses) {
        List<Report> reports = reportRepository.findByTarget(row.getTargetType(), row.getTargetId(), statuses);
        Map<ReportReason, Long> reasons = new EnumMap<>(ReportReason.class);
        reports.forEach(report -> reasons.merge(report.getReason(), 1L, Long::sum));
        List<ReportCaseResponse.Entry> entries = reports.stream()
                .map(report -> new ReportCaseResponse.Entry(report.getId(), report.getReason(), report.getDetails(),
                        report.getReporter().getName(), report.getCreatedAt(), report.getStatus(),
                        report.getResolution()))
                .toList();
        return new ReportCaseResponse(row.getTargetType(), row.getTargetId(),
                describe(row.getTargetType(), row.getTargetId()), row.getTotal(), reasons, row.getLastReportedAt(),
                entries);
    }

    private Target describe(ReportTargetType type, Long id) {
        return switch (type) {
            case DISCUSSION -> discussionRepository.findById(id).map(ReportServiceImpl::describe).orElseGet(Target::missing);
            case COMMENT -> commentRepository.findById(id).map(ReportServiceImpl::describe).orElseGet(Target::missing);
            case TESTIMONIAL -> testimonialRepository.findById(id).map(ReportServiceImpl::describe).orElseGet(Target::missing);
        };
    }

    private static Target describe(Discussion discussion) {
        return new Target(true, Boolean.TRUE.equals(discussion.getIsActive()), discussion.getTitle(),
                TextUtil.truncate(discussion.getContent(), EXCERPT_LENGTH), discussion.getAuthor().getId(),
                discussion.getAuthor().getName(), discussion.getId(), SlugUtil.slugify(discussion.getTitle()));
    }

    private static Target describe(Comment comment) {
        Discussion discussion = comment.getDiscussion();
        return new Target(true, Boolean.TRUE.equals(discussion.getIsActive()), discussion.getTitle(),
                TextUtil.truncate(comment.getContent(), EXCERPT_LENGTH), comment.getAuthor().getId(),
                comment.getAuthor().getName(), discussion.getId(), SlugUtil.slugify(discussion.getTitle()));
    }

    private static Target describe(Testimonial testimonial) {
        return new Target(true, isPublished(testimonial), testimonial.getName(),
                TextUtil.truncate(testimonial.getContent(), EXCERPT_LENGTH),
                testimonial.getUser() == null ? null : testimonial.getUser().getId(), testimonial.getName(), null, null);
    }
}
