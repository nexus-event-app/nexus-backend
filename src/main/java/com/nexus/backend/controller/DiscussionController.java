package com.nexus.backend.controller;

import com.nexus.backend.dto.request.CreateDiscussionRequest;
import com.nexus.backend.dto.request.UpdateDiscussionRequest;
import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.service.DiscussionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DiscussionController {
    
    private final DiscussionService discussionService;
    
    @PostMapping
    public ResponseEntity<DiscussionResponse> createDiscussion(
            @Valid @RequestBody CreateDiscussionRequest request,
            @RequestHeader("User-Id") Long userId) {
        DiscussionResponse response = discussionService.createDiscussion(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DiscussionResponse> updateDiscussion(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDiscussionRequest request,
            @RequestHeader("User-Id") Long userId) {
        DiscussionResponse response = discussionService.updateDiscussion(id, request, userId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscussion(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        discussionService.deleteDiscussion(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DiscussionResponse> getDiscussionById(
            @PathVariable Long id,
            @RequestHeader(value = "User-Id", required = false) Long userId) {
        DiscussionResponse response;
        if (userId != null) {
            response = discussionService.getDiscussionById(id, userId);
        } else {
            response = discussionService.getDiscussionById(id);
        }
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<DiscussionResponse>> getAllDiscussions(
            @RequestHeader(value = "User-Id", required = false) Long userId) {
        List<DiscussionResponse> discussions;
        if (userId != null) {
            discussions = discussionService.getAllDiscussions(userId);
        } else {
            discussions = discussionService.getAllDiscussions();
        }
        return ResponseEntity.ok(discussions);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<DiscussionResponse>> getDiscussionsByCategory(
            @PathVariable DiscussionCategory category,
            @RequestHeader(value = "User-Id", required = false) Long userId) {
        List<DiscussionResponse> discussions;
        if (userId != null) {
            discussions = discussionService.getDiscussionsByCategory(category, userId);
        } else {
            discussions = discussionService.getDiscussionsByCategory(category);
        }
        return ResponseEntity.ok(discussions);
    }
    
    @GetMapping("/my-discussions")
    public ResponseEntity<List<DiscussionResponse>> getMyDiscussions(
            @RequestHeader("User-Id") Long userId) {
        List<DiscussionResponse> discussions = discussionService.getMyDiscussions(userId);
        return ResponseEntity.ok(discussions);
    }
    
    @PostMapping("/{id}/like")
    public ResponseEntity<DiscussionResponse> toggleLike(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        DiscussionResponse response = discussionService.toggleLike(id, userId);
        return ResponseEntity.ok(response);
    }
}