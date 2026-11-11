package com.nexus.backend.dto.request;

import com.nexus.backend.enums.ReportReason;
import com.nexus.backend.enums.ReportTargetType;
import com.nexus.backend.util.TextUtil;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Reporte de un contenido: qué es, por qué y, si se quiere, más detalles para el equipo. */
public record CreateReportRequest(
        @NotNull(message = "{validation.report.target.required}") ReportTargetType targetType,
        @NotNull(message = "{validation.report.target.required}") Long targetId,
        @NotNull(message = "{validation.report.reason.required}") ReportReason reason,
        @Size(max = 500, message = "{validation.report.details.max500}") String details) {

    public CreateReportRequest {
        details = details == null || details.isBlank() ? null : TextUtil.clean(details);
    }
}
