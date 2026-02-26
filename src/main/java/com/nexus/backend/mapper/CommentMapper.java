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
        comment.setLikesCount(0);
        return comment;
    }
    
public CommentResponse toResponse(Comment comment, Boolean isLikedByCurrentUser) {
    CommentResponse response = new CommentResponse();
    response.setId(comment.getId());
    response.setContent(comment.getContent());
    response.setDiscussionId(comment.getDiscussion().getId());
    
    if (comment.getAuthor() != null) {
        response.setAuthorId(comment.getAuthor().getId());
        response.setAuthorName(comment.getAuthor().getName());
    } else {
        response.setAuthorId(null);
        response.setAuthorName("Anónimo");
    }
    
    response.setLikesCount(comment.getLikesCount());
    response.setIsLikedByCurrentUser(isLikedByCurrentUser);
    response.setCreatedAt(comment.getCreatedAt());
    response.setUpdatedAt(comment.getUpdatedAt());
    return response;
}
}