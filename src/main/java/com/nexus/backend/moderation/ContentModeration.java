package com.nexus.backend.moderation;

import com.nexus.backend.entity.Report;
import com.nexus.backend.enums.ReportReason;
import com.nexus.backend.enums.ReportTargetType;
import com.nexus.backend.exception.BadRequestException;
import com.nexus.backend.i18n.Messages;
import com.nexus.backend.mail.ModerationAlerts;
import com.nexus.backend.moderation.ContentFilter.Level;
import com.nexus.backend.moderation.ContentFilter.Verdict;
import com.nexus.backend.repository.ReportRepository;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentModeration {

    private static final Locale MODERATION_LOCALE = Locale.forLanguageTag("es");
    private static final int DETAILS_LENGTH = 500;

    private final ReportRepository reportRepository;
    private final ModerationAlerts moderationAlerts;
    private final Messages messages;

    public List<String> wordsToHold(UserDetail actor, String... texts) {
        Verdict verdict = ContentFilter.check(texts);
        if (verdict.level() == Level.BLOCK) {
            throw new BadRequestException("error.content.blocked");
        }
        return verdict.level() == Level.HOLD && !actor.isAdmin() ? verdict.words() : List.of();
    }

    public void requireClean(String... texts) {
        if (!ContentFilter.check(texts).isClean()) {
            throw new BadRequestException("error.profile.wordsNotAllowed");
        }
    }

    public void holdForReview(ReportTargetType type, Long id, String title, String content, List<String> words) {
        String reason = messages.get(MODERATION_LOCALE, "moderation.held.words", String.join(", ", words));
        Report report = new Report();
        report.setTargetType(type);
        report.setTargetId(id);
        report.setReason(ReportReason.LANGUAGE);
        report.setDetails(TextUtil.truncate(reason, DETAILS_LENGTH));
        reportRepository.save(report);
        moderationAlerts.heldForReview(title, content, reason);
        log.info("{} id={} retenido por el filtro de lenguaje", type, id);
    }

    public void hiddenByReports(String title, String content, int reports) {
        moderationAlerts.heldForReview(title, content,
                messages.get(MODERATION_LOCALE, "moderation.held.reports", reports));
    }
}
