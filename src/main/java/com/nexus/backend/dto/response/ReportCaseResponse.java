package com.nexus.backend.dto.response;

import com.nexus.backend.enums.ReportAction;
import com.nexus.backend.enums.ReportReason;
import com.nexus.backend.enums.ReportStatus;
import com.nexus.backend.enums.ReportTargetType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ReportCaseResponse(
        ReportTargetType targetType,
        Long targetId,
        Target target,
        long total,
        Map<ReportReason, Long> reasons,
        LocalDateTime lastReportedAt,
        List<Entry> reports) {

    public record Target(boolean exists, boolean visible, String title, String excerpt, Long authorId,
                         String authorName, Long discussionId, String discussionSlug) {

        public static Target missing() {
            return new Target(false, false, null, null, null, null, null, null);
        }
    }

    public record Entry(Long id, ReportReason reason, String details, String reporterName, LocalDateTime createdAt,
                        ReportStatus status, ReportAction resolution) {
    }
}
