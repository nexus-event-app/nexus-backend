package com.nexus.backend.controller;

import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.DiscussionService;
import com.nexus.backend.util.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/discussions")
@RequiredArgsConstructor
public class AdminDiscussionController {

    private final DiscussionService discussionService;

    @GetMapping
    public ResponseEntity<PageResponse<DiscussionResponse>> getAllDiscussions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String q,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(discussionService.getAllDiscussions(q, page, size, principal));
    }
}
