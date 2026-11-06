package com.nexus.backend.controller;

import com.nexus.backend.dto.request.CreateJobRequest;
import com.nexus.backend.dto.request.UpdateJobRequest;
import com.nexus.backend.dto.response.JobResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.WorkMode;
import com.nexus.backend.service.JobService;
import com.nexus.backend.util.Pagination;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
public class AdminJobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<PageResponse<JobResponse>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_SIZE) int size,
            @RequestParam(required = false) JobLevel level,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) ContractType contractType,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(jobService.getAllJobs(level, workMode, contractType, q, page, size));
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody CreateJobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable Long id, @Valid @RequestBody UpdateJobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
