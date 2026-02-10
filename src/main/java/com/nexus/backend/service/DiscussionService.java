package com.nexus.backend.service;

import com.nexus.backend.dto.request.CreateDiscussionRequest;
import com.nexus.backend.dto.request.UpdateDiscussionRequest;
import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.enums.DiscussionCategory;

import java.util.List;

public interface DiscussionService {
    
    DiscussionResponse createDiscussion(CreateDiscussionRequest request, Long userId);
    
    DiscussionResponse updateDiscussion(Long id, UpdateDiscussionRequest request, Long userId);
    
    void deleteDiscussion(Long id, Long userId);
    
    DiscussionResponse getDiscussionById(Long id);
    
    List<DiscussionResponse> getAllDiscussions();
    
    List<DiscussionResponse> getDiscussionsByCategory(DiscussionCategory category);
    
    List<DiscussionResponse> getMyDiscussions(Long userId);
}