package com.nexus.backend.controller;

import com.nexus.backend.dto.request.ResolveReportRequest;
import com.nexus.backend.dto.response.ReportCaseResponse;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Cola de moderación: contenidos reportados y decisiones del equipo. */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    /** {@code status=pending} (por defecto) o {@code status=closed}. */
    @GetMapping
    public ResponseEntity<List<ReportCaseResponse>> getCases(@RequestParam(defaultValue = "pending") String status) {
        return ResponseEntity.ok(reportService.getCases(!"closed".equalsIgnoreCase(status)));
    }

    @PostMapping("/resolve")
    public ResponseEntity<Void> resolve(@Valid @RequestBody ResolveReportRequest request,
                                        @AuthenticationPrincipal UserDetail principal) {
        reportService.resolve(request, principal);
        return ResponseEntity.noContent().build();
    }
}
