package com.nexus.backend.controller;

import com.nexus.backend.dto.request.CreateCommentRequest;
import com.nexus.backend.dto.response.CommentResponse;
import com.nexus.backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CommentController {
    
    private final CommentService commentService;
    
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            @RequestHeader("User-Id") Long userId) {
        CommentResponse response = commentService.createComment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        commentService.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/discussion/{discussionId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByDiscussion(
            @PathVariable Long discussionId) {
        List<CommentResponse> comments = commentService.getCommentsByDiscussion(discussionId);
        return ResponseEntity.ok(comments);
    }
}