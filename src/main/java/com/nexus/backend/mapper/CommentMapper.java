package com.nexus.backend.mapper;

import com.nexus.backend.dto.request.CreateCommentRequest;
import com.nexus.backend.dto.response.CommentResponse;
import com.nexus.backend.entity.Comment;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    
    public Comment toEntity(CreateCommentRequest request, Discussion discussion, User author) {
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setDiscussion(discussion);
        comment.setAuthor(author);
        return comment;
    }
    
    public CommentResponse toResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setDiscussionId(comment.getDiscussion().getId());
        response.setAuthorId(comment.getAuthor().getId());
        response.setAuthorName(comment.getAuthor().getName());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }
}