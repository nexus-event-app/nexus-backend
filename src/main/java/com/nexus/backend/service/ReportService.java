package com.nexus.backend.service;

import com.nexus.backend.dto.request.CreateReportRequest;
import com.nexus.backend.dto.request.ResolveReportRequest;
import com.nexus.backend.dto.response.ReportCaseResponse;
import com.nexus.backend.security.UserDetail;

import java.util.List;

public interface ReportService {

    void create(CreateReportRequest request, UserDetail actor);

    List<ReportCaseResponse> getCases(boolean pending);

    void resolve(ResolveReportRequest request, UserDetail actor);
}
