package com.nexus.backend.controller;

import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.dto.response.SavedJobResponse;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.SavedJobService;
import com.nexus.backend.util.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
public class SavedJobController {

    private final SavedJobService savedJobService;

    @GetMapping
    public ResponseEntity<PageResponse<SavedJobResponse>> getSavedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_SIZE) int size,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(savedJobService.getSavedJobs(page, size, principal));
    }

    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getSavedJobIds(@AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(savedJobService.getSavedJobIds(principal));
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<Void> save(@PathVariable Long jobId, @AuthenticationPrincipal UserDetail principal) {
        savedJobService.save(jobId, principal);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> remove(@PathVariable Long jobId, @AuthenticationPrincipal UserDetail principal) {
        savedJobService.remove(jobId, principal);
        return ResponseEntity.noContent().build();
    }
}
