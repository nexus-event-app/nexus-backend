package com.nexus.backend.controller;

import com.nexus.backend.dto.request.CreateDiscussionRequest;
import com.nexus.backend.dto.request.UpdateDiscussionRequest;
import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.DiscussionService;
import com.nexus.backend.util.Pagination;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    @GetMapping
    public ResponseEntity<PageResponse<DiscussionResponse>> getDiscussions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_SIZE) int size,
            @RequestParam(required = false) DiscussionCategory category,
            @RequestParam(required = false) String q,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(discussionService.getDiscussions(category, q, page, size, principal));
    }

    @GetMapping("/mine")
    public ResponseEntity<PageResponse<DiscussionResponse>> getMyDiscussions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_SIZE) int size,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(discussionService.getMyDiscussions(page, size, principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscussionResponse> getDiscussionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(discussionService.getDiscussionById(id, principal));
    }

    @PostMapping
    public ResponseEntity<DiscussionResponse> createDiscussion(
            @Valid @RequestBody CreateDiscussionRequest request,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(discussionService.createDiscussion(request, principal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscussionResponse> updateDiscussion(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDiscussionRequest request,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(discussionService.updateDiscussion(id, request, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscussion(@PathVariable Long id, @AuthenticationPrincipal UserDetail principal) {
        discussionService.deleteDiscussion(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<DiscussionResponse> toggleLike(@PathVariable Long id, @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(discussionService.toggleLike(id, principal));
    }
}
