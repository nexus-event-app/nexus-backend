package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateDiscussionRequest;
import com.nexus.backend.dto.request.UpdateDiscussionRequest;
import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.DiscussionLike;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.mapper.DiscussionMapper;
import com.nexus.backend.repository.CommentRepository;
import com.nexus.backend.repository.DiscussionLikeRepository;
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
    private final DiscussionLikeRepository discussionLikeRepository;
    private final DiscussionMapper discussionMapper;
    
    @Override
    @Transactional
    public DiscussionResponse createDiscussion(CreateDiscussionRequest request, Long userId) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        
        Discussion discussion = discussionMapper.toEntity(request, user);
        Discussion saved = discussionRepository.save(discussion);
        
        return discussionMapper.toResponse(saved, 0, false);
    }
    
    @Override
    @Transactional
    public DiscussionResponse updateDiscussion(Long id, UpdateDiscussionRequest request, Long userId) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discusión no encontrada"));
        
        if (discussion.getAuthor() != null && userId != null) {
            if (!discussion.getAuthor().getId().equals(userId)) {
                throw new RuntimeException("No tienes permiso para editar esta discusión");
            }
        } else if (discussion.getAuthor() == null && userId == null) {
    
        } else {
            throw new RuntimeException("No tienes permiso para editar esta discusión");
        }
        
        discussionMapper.updateEntity(discussion, request);
        Discussion updated = discussionRepository.save(discussion);
        
        Integer commentCount = commentRepository.countByDiscussionId(id).intValue();
        boolean isLiked = userId != null && discussionLikeRepository.existsByDiscussionIdAndUserId(id, userId);
        return discussionMapper.toResponse(updated, commentCount, isLiked);
    }
    
    @Override
    @Transactional
    public void deleteDiscussion(Long id, Long userId) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discusión no encontrada"));
        
        boolean isAdmin = false;
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                isAdmin = "ADMIN".equals(user.getRole().name());
            }
        }
        
        boolean isAuthor = discussion.getAuthor() != null && userId != null 
                && discussion.getAuthor().getId().equals(userId);
        
        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("No tienes permiso para eliminar esta discusión");
        }
        
        commentRepository.findByDiscussionIdOrderByCreatedAtAsc(id)
                .forEach(comment -> commentRepository.delete(comment));
        
        discussionRepository.delete(discussion);
    }
    
    @Override
    public DiscussionResponse getDiscussionById(Long id) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discusión no encontrada"));
        
        Integer commentCount = commentRepository.countByDiscussionId(id).intValue();
        return discussionMapper.toResponse(discussion, commentCount, false);
    }
    
    @Override
    public DiscussionResponse getDiscussionById(Long id, Long userId) {
        Discussion discussion = discussionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discusión no encontrada"));
        
        Integer commentCount = commentRepository.countByDiscussionId(id).intValue();
        boolean isLiked = discussionLikeRepository.existsByDiscussionIdAndUserId(id, userId);
        return discussionMapper.toResponse(discussion, commentCount, isLiked);
    }
    
    @Override
    public List<DiscussionResponse> getAllDiscussions() {
        return discussionRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(discussion -> {
                    Integer commentCount = commentRepository.countByDiscussionId(discussion.getId()).intValue();
                    return discussionMapper.toResponse(discussion, commentCount, false);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DiscussionResponse> getAllDiscussions(Long userId) {
        return discussionRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(discussion -> {
                    Integer commentCount = commentRepository.countByDiscussionId(discussion.getId()).intValue();
                    boolean isLiked = discussionLikeRepository.existsByDiscussionIdAndUserId(discussion.getId(), userId);
                    return discussionMapper.toResponse(discussion, commentCount, isLiked);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DiscussionResponse> getDiscussionsByCategory(DiscussionCategory category) {
        return discussionRepository.findByIsActiveTrueAndCategoryOrderByCreatedAtDesc(category)
                .stream()
                .map(discussion -> {
                    Integer commentCount = commentRepository.countByDiscussionId(discussion.getId()).intValue();
                    return discussionMapper.toResponse(discussion, commentCount, false);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DiscussionResponse> getDiscussionsByCategory(DiscussionCategory category, Long userId) {
        return discussionRepository.findByIsActiveTrueAndCategoryOrderByCreatedAtDesc(category)
                .stream()
                .map(discussion -> {
                    Integer commentCount = commentRepository.countByDiscussionId(discussion.getId()).intValue();
                    boolean isLiked = discussionLikeRepository.existsByDiscussionIdAndUserId(discussion.getId(), userId);
                    return discussionMapper.toResponse(discussion, commentCount, isLiked);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DiscussionResponse> getMyDiscussions(Long userId) {
        return discussionRepository.findByAuthorIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(discussion -> {
                    Integer commentCount = commentRepository.countByDiscussionId(discussion.getId()).intValue();
                    boolean isLiked = discussionLikeRepository.existsByDiscussionIdAndUserId(discussion.getId(), userId);
                    return discussionMapper.toResponse(discussion, commentCount, isLiked);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public DiscussionResponse toggleLike(Long discussionId, Long userId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new RuntimeException("Discusión no encontrada"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        boolean isLiked = discussionLikeRepository.existsByDiscussionIdAndUserId(discussionId, userId);
        
        if (isLiked) {
            discussionLikeRepository.deleteByDiscussionIdAndUserId(discussionId, userId);
            discussion.setLikesCount(discussion.getLikesCount() - 1);
        } else {
            DiscussionLike like = new DiscussionLike();
            like.setDiscussion(discussion);
            like.setUser(user);
            discussionLikeRepository.save(like);
            discussion.setLikesCount(discussion.getLikesCount() + 1);
        }
        
        discussionRepository.save(discussion);
        
        Integer commentCount = commentRepository.countByDiscussionId(discussionId).intValue();
        return discussionMapper.toResponse(discussion, commentCount, !isLiked);
    }
}