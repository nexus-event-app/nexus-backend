package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateCommentRequest;
import com.nexus.backend.dto.response.CommentResponse;
import com.nexus.backend.entity.Comment;
import com.nexus.backend.entity.CommentLike;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.User;
import com.nexus.backend.mapper.CommentMapper;
import com.nexus.backend.repository.CommentLikeRepository;
import com.nexus.backend.repository.CommentRepository;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.service.CommentService;
import com.nexus.backend.util.HtmlSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    
    private final CommentRepository commentRepository;
    private final DiscussionRepository discussionRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentMapper commentMapper;
    private final HtmlSanitizer htmlSanitizer;
    
    @Override
    @Transactional
    public CommentResponse createComment(CreateCommentRequest request, Long userId) {
        Discussion discussion = discussionRepository.findById(request.getDiscussionId())
                .orElseThrow(() -> new RuntimeException("Discusión no encontrada"));
        
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        
        request.setContent(htmlSanitizer.sanitize(request.getContent()));
        
        Comment comment = commentMapper.toEntity(request, discussion, user);
        Comment saved = commentRepository.save(comment);
        
        return commentMapper.toResponse(saved, false);
    }
    
    @Override
    @Transactional
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
        
        boolean isAdmin = false;
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                isAdmin = "ADMIN".equals(user.getRole().name());
            }
        }
        
        boolean isAuthor = comment.getAuthor() != null && userId != null 
                && comment.getAuthor().getId().equals(userId);
        
        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("No tienes permiso para eliminar este comentario");
        }
        
        commentRepository.delete(comment);
    }
    
    @Override
    public List<CommentResponse> getCommentsByDiscussion(Long discussionId) {
        return commentRepository.findByDiscussionIdOrderByCreatedAtAsc(discussionId)
                .stream()
                .map(comment -> commentMapper.toResponse(comment, false))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CommentResponse> getCommentsByDiscussion(Long discussionId, Long userId) {
        return commentRepository.findByDiscussionIdOrderByCreatedAtAsc(discussionId)
                .stream()
                .map(comment -> {
                    boolean isLiked = commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), userId);
                    return commentMapper.toResponse(comment, isLiked);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public CommentResponse toggleLike(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        boolean isLiked = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
        
        if (isLiked) {
            commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
            comment.setLikesCount(comment.getLikesCount() - 1);
        } else {
            CommentLike like = new CommentLike();
            like.setComment(comment);
            like.setUser(user);
            commentLikeRepository.save(like);
            comment.setLikesCount(comment.getLikesCount() + 1);
        }
        
        commentRepository.save(comment);
        
        return commentMapper.toResponse(comment, !isLiked);
    }
}