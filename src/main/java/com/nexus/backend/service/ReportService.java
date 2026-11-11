package com.nexus.backend.service;

import com.nexus.backend.dto.request.CreateReportRequest;
import com.nexus.backend.dto.request.ResolveReportRequest;
import com.nexus.backend.dto.response.ReportCaseResponse;
import com.nexus.backend.security.UserDetail;

import java.util.List;

public interface ReportService {

    /** Reporta un contenido publicado de otra persona. Cada persona puede reportar cada contenido una vez. */
    void create(CreateReportRequest request, UserDetail actor);

    /** Contenidos reportados, agrupados: los pendientes de revisar o, con {@code pending} false, los ya cerrados. */
    List<ReportCaseResponse> getCases(boolean pending);

    /** Oculta, borra o descarta un contenido reportado y cierra sus reportes pendientes. */
    void resolve(ResolveReportRequest request, UserDetail actor);
}
