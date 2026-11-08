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

    public CommentResponse toResponse(Comment comment, int likesCount, boolean likedByCurrentUser) {
        User author = comment.getAuthor();
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .discussionId(comment.getDiscussion().getId())
                .authorId(author.getId())
                .authorName(author.getName())
                .likesCount(likesCount)
                .isLikedByCurrentUser(likedByCurrentUser)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
