package com.nexus.backend.mapper;

import com.nexus.backend.dto.request.CreateDiscussionRequest;
import com.nexus.backend.dto.request.UpdateDiscussionRequest;
import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.User;
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
        return discussion;
    }
    
    public void updateEntity(Discussion discussion, UpdateDiscussionRequest request) {
        if (request.getTitle() != null) {
            discussion.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            discussion.setContent(request.getContent());
        }
        if (request.getCategory() != null) {
            discussion.setCategory(request.getCategory());
        }
        if (request.getIsActive() != null) {
            discussion.setIsActive(request.getIsActive());
        }
    }
    
    public DiscussionResponse toResponse(Discussion discussion, Integer commentCount) {
        DiscussionResponse response = new DiscussionResponse();
        response.setId(discussion.getId());
        response.setTitle(discussion.getTitle());
        response.setContent(discussion.getContent());
        response.setCategory(discussion.getCategory());
        response.setAuthorId(discussion.getAuthor().getId());
        response.setAuthorName(discussion.getAuthor().getName());
        response.setIsActive(discussion.getIsActive());
        response.setCommentCount(commentCount);
        response.setCreatedAt(discussion.getCreatedAt());
        response.setUpdatedAt(discussion.getUpdatedAt());
        return response;
    }
}