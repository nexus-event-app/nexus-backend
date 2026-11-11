package com.nexus.backend.dto.request;

import com.nexus.backend.enums.ReportAction;
import com.nexus.backend.enums.ReportTargetType;
import jakarta.validation.constraints.NotNull;

/** Decisión del equipo sobre un contenido reportado. */
public record ResolveReportRequest(
        @NotNull(message = "{validation.report.target.required}") ReportTargetType targetType,
        @NotNull(message = "{validation.report.target.required}") Long targetId,
        @NotNull(message = "{validation.report.action.required}") ReportAction action) {
}
