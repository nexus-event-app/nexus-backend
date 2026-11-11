package com.nexus.backend.controller;

import com.nexus.backend.dto.request.CreateReportRequest;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Reportes de la comunidad sobre contenido que incumple las normas. */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateReportRequest request,
                                       @AuthenticationPrincipal UserDetail principal) {
        reportService.create(request, principal);
        return ResponseEntity.noContent().build();
    }
}
