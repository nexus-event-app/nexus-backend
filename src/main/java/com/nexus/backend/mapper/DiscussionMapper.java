package com.nexus.backend.mapper;

import com.nexus.backend.dto.request.CreateDiscussionRequest;
import com.nexus.backend.dto.request.UpdateDiscussionRequest;
import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.User;
import com.nexus.backend.util.SlugUtil;
import org.springframework.stereotype.Component;

@Component
public class DiscussionMapper {

    public Discussion toEntity(CreateDiscussionRequest request, User author) {
        Discussion discussion = new Discussion();
        discussion.setTitle(request.getTitle());
        discussion.setContent(request.getContent());
        discussion.setCategory(request.getCategory());
        discussion.setAuthor(author);
        discussion.setIsActive(true);
        discussion.setLikesCount(0);
        return discussion;
    }

    public void updateEntity(Discussion discussion, UpdateDiscussionRequest request) {
        discussion.setTitle(request.getTitle());
        discussion.setContent(request.getContent());
        discussion.setCategory(request.getCategory());
    }

    public DiscussionResponse toResponse(Discussion discussion, int commentCount, int likesCount, boolean likedByCurrentUser) {
        User author = discussion.getAuthor();
        return DiscussionResponse.builder()
                .id(discussion.getId())
                .slug(SlugUtil.slugify(discussion.getTitle()))
                .title(discussion.getTitle())
                .content(discussion.getContent())
                .category(discussion.getCategory())
                .authorId(author.getId())
                .authorName(author.getName())
                .authorAvatarUrl(author.getAvatarUrl())
                .isActive(discussion.getIsActive())
                .commentCount(commentCount)
                .likesCount(likesCount)
                .acceptedCommentId(discussion.getAcceptedCommentId())
                .isLikedByCurrentUser(likedByCurrentUser)
                .createdAt(discussion.getCreatedAt())
                .updatedAt(discussion.getUpdatedAt())
                .build();
    }
}
