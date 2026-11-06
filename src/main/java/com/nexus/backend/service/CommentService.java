package com.nexus.backend.service;

import com.nexus.backend.dto.request.CreateCommentRequest;
import com.nexus.backend.dto.response.CommentResponse;
import com.nexus.backend.security.UserDetail;

import java.util.List;

public interface CommentService {

    /** {@code viewer} puede ser null (visitante anónimo). */
    List<CommentResponse> getCommentsByDiscussion(Long discussionId, UserDetail viewer);

    CommentResponse createComment(CreateCommentRequest request, UserDetail actor);

    void deleteComment(Long id, UserDetail actor);

    CommentResponse toggleLike(Long id, UserDetail actor);
}
