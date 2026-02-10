package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateDiscussionRequest;
import com.nexus.backend.dto.request.UpdateDiscussionRequest;
import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.mapper.DiscussionMapper;
import com.nexus.backend.repository.CommentRepository;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.service.DiscussionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscussionServiceImpl implements DiscussionService {
    
    private final DiscussionRepository discussionRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final DiscussionMapper discussionMapper;
    
    @Override
    @Transactional
    public DiscussionResponse createDiscussion(CreateDiscussionRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Discussion discussion = discussionMapper.toEntity(request, user);
        Discussion saved = discussionRepository.save(discussion);
        
        return discussionMapper.toResponse(saved, 0);
    }
    
    @Override
    @Transactional
    public DiscussionResponse updateDiscussion(Long id, UpdateDiscussionRequest request, Long userId) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discusión no encontrada"));
        
        if (!discussion.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para editar esta discusión");
        }
        
        discussionMapper.updateEntity(discussion, request);
        Discussion updated = discussionRepository.save(discussion);
        
        Integer commentCount = commentRepository.countByDiscussionId(id).intValue();
        return discussionMapper.toResponse(updated, commentCount);
    }
    
    @Override
    @Transactional
    public void deleteDiscussion(Long id, Long userId) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discusión no encontrada"));
        
        if (!discussion.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar esta discusión");
        }
        
        discussionRepository.delete(discussion);
    }
    
    @Override
    public DiscussionResponse getDiscussionById(Long id) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discusión no encontrada"));
        
        Integer commentCount = commentRepository.countByDiscussionId(id).intValue();
        return discussionMapper.toResponse(discussion, commentCount);
    }
    
    @Override
    public List<DiscussionResponse> getAllDiscussions() {
        return discussionRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(discussion -> {
                    Integer commentCount = commentRepository.countByDiscussionId(discussion.getId()).intValue();
                    return discussionMapper.toResponse(discussion, commentCount);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DiscussionResponse> getDiscussionsByCategory(DiscussionCategory category) {
        return discussionRepository.findByIsActiveTrueAndCategoryOrderByCreatedAtDesc(category)
                .stream()
                .map(discussion -> {
                    Integer commentCount = commentRepository.countByDiscussionId(discussion.getId()).intValue();
                    return discussionMapper.toResponse(discussion, commentCount);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DiscussionResponse> getMyDiscussions(Long userId) {
        return discussionRepository.findByAuthorIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(discussion -> {
                    Integer commentCount = commentRepository.countByDiscussionId(discussion.getId()).intValue();
                    return discussionMapper.toResponse(discussion, commentCount);
                })
                .collect(Collectors.toList());
    }
}