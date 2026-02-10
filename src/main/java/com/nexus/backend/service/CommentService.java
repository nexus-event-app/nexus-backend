package com.nexus.backend.service;

import com.nexus.backend.dto.request.CreateCommentRequest;
import com.nexus.backend.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {
    
    CommentResponse createComment(CreateCommentRequest request, Long userId);
    
    void deleteComment(Long id, Long userId);
    
    List<CommentResponse> getCommentsByDiscussion(Long discussionId);
}