package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateCommentRequest;
import com.nexus.backend.dto.response.CommentResponse;
import com.nexus.backend.entity.Comment;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.User;
import com.nexus.backend.mapper.CommentMapper;
import com.nexus.backend.repository.CommentRepository;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.service.CommentService;
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
    private final CommentMapper commentMapper;
    
    @Override
    @Transactional
    public CommentResponse createComment(CreateCommentRequest request, Long userId) {
        Discussion discussion = discussionRepository.findById(request.getDiscussionId())
                .orElseThrow(() -> new RuntimeException("Discusión no encontrada"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Comment comment = commentMapper.toEntity(request, discussion, user);
        Comment saved = commentRepository.save(comment);
        
        return commentMapper.toResponse(saved);
    }
    
    @Override
    @Transactional
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
        
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar este comentario");
        }
        
        commentRepository.delete(comment);
    }
    
    @Override
    public List<CommentResponse> getCommentsByDiscussion(Long discussionId) {
        return commentRepository.findByDiscussionIdOrderByCreatedAtAsc(discussionId)
                .stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }
}