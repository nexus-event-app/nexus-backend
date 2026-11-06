package com.nexus.backend.controller;

import com.nexus.backend.dto.request.CreateCommentRequest;
import com.nexus.backend.dto.response.CommentResponse;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/discussion/{discussionId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByDiscussion(
            @PathVariable Long discussionId,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(commentService.getCommentsByDiscussion(discussionId, principal));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(request, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, @AuthenticationPrincipal UserDetail principal) {
        commentService.deleteComment(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<CommentResponse> toggleLike(@PathVariable Long id, @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(commentService.toggleLike(id, principal));
    }
}
