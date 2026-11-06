package com.nexus.backend.controller;

import com.nexus.backend.dto.response.JobResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.WorkMode;
import com.nexus.backend.service.JobService;
import com.nexus.backend.util.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<PageResponse<JobResponse>> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_SIZE) int size,
            @RequestParam(required = false) JobLevel level,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) ContractType contractType,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(jobService.getPublicJobs(level, workMode, contractType, q, page, size));
    }

    @GetMapping("/latest")
    public ResponseEntity<List<JobResponse>> getLatestJobs(
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_LATEST) int limit) {
        return ResponseEntity.ok(jobService.getLatestJobs(limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getPublicJobById(id));
    }
}
