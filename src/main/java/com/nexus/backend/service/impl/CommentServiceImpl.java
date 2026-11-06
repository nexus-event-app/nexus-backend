package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateCommentRequest;
import com.nexus.backend.dto.response.CommentResponse;
import com.nexus.backend.entity.Comment;
import com.nexus.backend.entity.CommentLike;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.User;
import com.nexus.backend.exception.ForbiddenOperationException;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.exception.UnauthorizedException;
import com.nexus.backend.mapper.CommentMapper;
import com.nexus.backend.repository.CommentLikeRepository;
import com.nexus.backend.repository.CommentRepository;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final String DISCUSSION_NOT_FOUND = "Discusión no encontrada";
    private static final String COMMENT_NOT_FOUND = "Comentario no encontrado";

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final DiscussionRepository discussionRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByDiscussion(Long discussionId, UserDetail viewer) {
        findActiveDiscussion(discussionId);
        List<Comment> comments = commentRepository.findByDiscussionIdOrderByCreatedAtAsc(discussionId);

        Set<Long> likedIds = new HashSet<>();
        if (viewer != null && !comments.isEmpty()) {
            List<Long> ids = comments.stream().map(Comment::getId).toList();
            likedIds.addAll(commentLikeRepository.findLikedCommentIds(viewer.getId(), ids));
        }

        return comments.stream()
                .map(comment -> commentMapper.toResponse(comment, comment.getLikesCount(), likedIds.contains(comment.getId())))
                .toList();
    }

    @Override
    @Transactional
    public CommentResponse createComment(CreateCommentRequest request, UserDetail actor) {
        Discussion discussion = findActiveDiscussion(request.getDiscussionId());
        User author = userRepository.findById(actor.getId())
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        Comment saved = commentRepository.save(commentMapper.toEntity(request, discussion, author));
        return commentMapper.toResponse(saved, 0, false);
    }

    @Override
    @Transactional
    public void deleteComment(Long id, UserDetail actor) {
        Comment comment = findComment(id);
        Long ownerId = comment.getAuthor() != null ? comment.getAuthor().getId() : null;
        if (!actor.canManage(ownerId)) {
            throw new ForbiddenOperationException("Solo el autor o un administrador puede eliminar el comentario");
        }

        commentLikeRepository.deleteAllByCommentId(id);
        commentRepository.delete(comment);
        log.info("Comentario id={} eliminado por usuario id={}", id, actor.getId());
    }

    @Override
    @Transactional
    public CommentResponse toggleLike(Long id, UserDetail actor) {
        Comment comment = findComment(id);
        Optional<CommentLike> existing = commentLikeRepository.findByCommentIdAndUserId(id, actor.getId());

        boolean liked;
        if (existing.isPresent()) {
            commentLikeRepository.delete(existing.get());
            liked = false;
        } else {
            CommentLike like = new CommentLike();
            like.setComment(comment);
            like.setUser(userRepository.findById(actor.getId())
                    .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado")));
            commentLikeRepository.save(like);
            liked = true;
        }

        int likesCount = (int) commentLikeRepository.countByCommentId(id);
        commentRepository.updateLikesCount(id, likesCount);
        return commentMapper.toResponse(comment, likesCount, liked);
    }

    private Comment findComment(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(COMMENT_NOT_FOUND));
    }

    private Discussion findActiveDiscussion(Long discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException(DISCUSSION_NOT_FOUND));
        if (!Boolean.TRUE.equals(discussion.getIsActive())) {
            throw new ResourceNotFoundException(DISCUSSION_NOT_FOUND);
        }
        return discussion;
    }
}
