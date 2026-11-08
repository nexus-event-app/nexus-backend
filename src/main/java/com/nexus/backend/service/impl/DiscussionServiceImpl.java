package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateDiscussionRequest;
import com.nexus.backend.dto.request.UpdateDiscussionRequest;
import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.DiscussionLike;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.exception.ForbiddenOperationException;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.exception.UnauthorizedException;
import com.nexus.backend.mapper.DiscussionMapper;
import com.nexus.backend.repository.CommentLikeRepository;
import com.nexus.backend.repository.CommentRepository;
import com.nexus.backend.repository.DiscussionLikeRepository;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.repository.projection.DiscussionCommentCount;
import com.nexus.backend.repository.spec.DiscussionSpecifications;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.AccountService;
import com.nexus.backend.service.DiscussionService;
import com.nexus.backend.util.Pagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscussionServiceImpl implements DiscussionService {

    private static final String NOT_FOUND_MESSAGE = "Discusión no encontrada";

    private final AccountService accountService;
    private final DiscussionRepository discussionRepository;
    private final DiscussionLikeRepository discussionLikeRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;
    private final DiscussionMapper discussionMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DiscussionResponse> getDiscussions(DiscussionCategory category, String text, int page, int size,
                                                           UserDetail viewer) {
        Specification<Discussion> spec = DiscussionSpecifications.isActive()
                .and(DiscussionSpecifications.filter(category, text));
        return findPage(spec, Pagination.newestFirst(page, size), viewer);
    }

    @Override
    @Transactional(readOnly = true)
    public DiscussionResponse getDiscussionById(Long id, UserDetail viewer) {
        Discussion discussion = findActive(id);
        boolean liked = viewer != null && discussionLikeRepository
                .findByDiscussionIdAndUserId(id, viewer.getId()).isPresent();
        return toResponse(discussion, liked);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DiscussionResponse> getMyDiscussions(int page, int size, UserDetail actor) {
        Specification<Discussion> spec = DiscussionSpecifications.isActive()
                .and(DiscussionSpecifications.hasAuthor(actor.getId()));
        return findPage(spec, Pagination.newestFirst(page, size), actor);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DiscussionResponse> getAllDiscussions(String text, int page, int size, UserDetail actor) {
        Specification<Discussion> spec = DiscussionSpecifications.filter(null, text);
        return findPage(spec, Pagination.newestFirst(page, size), actor);
    }

    @Override
    @Transactional
    public DiscussionResponse createDiscussion(CreateDiscussionRequest request, UserDetail actor) {
        accountService.assertCanParticipate(actor);
        User author = loadUser(actor);
        Discussion saved = discussionRepository.save(discussionMapper.toEntity(request, author));
        return discussionMapper.toResponse(saved, 0, 0, false);
    }

    @Override
    @Transactional
    public DiscussionResponse updateDiscussion(Long id, UpdateDiscussionRequest request, UserDetail actor) {
        Discussion discussion = findById(id);
        assertCanManage(discussion, actor);

        discussionMapper.updateEntity(discussion, request);
        if (actor.isAdmin() && request.getIsActive() != null) {
            discussion.setIsActive(request.getIsActive());
        }
        Discussion saved = discussionRepository.save(discussion);
        boolean liked = discussionLikeRepository.findByDiscussionIdAndUserId(id, actor.getId()).isPresent();
        return toResponse(saved, liked);
    }

    @Override
    @Transactional
    public void deleteDiscussion(Long id, UserDetail actor) {
        Discussion discussion = findById(id);
        assertCanManage(discussion, actor);

        commentLikeRepository.deleteAllByDiscussionId(id);
        commentRepository.deleteAllByDiscussionId(id);
        discussionLikeRepository.deleteAllByDiscussionId(id);
        discussionRepository.delete(discussion);
        log.info("Discusión id={} eliminada por usuario id={}", id, actor.getId());
    }

    @Override
    @Transactional
    public DiscussionResponse toggleLike(Long id, UserDetail actor) {
        Discussion discussion = findActive(id);
        Optional<DiscussionLike> existing = discussionLikeRepository.findByDiscussionIdAndUserId(id, actor.getId());

        boolean liked;
        if (existing.isPresent()) {
            discussionLikeRepository.delete(existing.get());
            liked = false;
        } else {
            DiscussionLike like = new DiscussionLike();
            like.setDiscussion(discussion);
            like.setUser(loadUser(actor));
            discussionLikeRepository.save(like);
            liked = true;
        }

        int likesCount = (int) discussionLikeRepository.countByDiscussionId(id);
        discussionRepository.updateLikesCount(id, likesCount);
        int commentCount = (int) commentRepository.countByDiscussionId(id);
        return discussionMapper.toResponse(discussion, commentCount, likesCount, liked);
    }

    private PageResponse<DiscussionResponse> findPage(Specification<Discussion> spec, Pageable pageable, UserDetail viewer) {
        Page<Discussion> result = discussionRepository.findAll(spec, pageable);
        List<Discussion> discussions = result.getContent();
        List<Long> ids = discussions.stream().map(Discussion::getId).toList();

        Map<Long, Long> commentCounts = loadCommentCounts(ids);
        Set<Long> likedIds = loadLikedIds(ids, viewer);

        List<DiscussionResponse> content = discussions.stream()
                .map(discussion -> discussionMapper.toResponse(
                        discussion,
                        commentCounts.getOrDefault(discussion.getId(), 0L).intValue(),
                        discussion.getLikesCount(),
                        likedIds.contains(discussion.getId())))
                .toList();
        return PageResponse.from(new PageImpl<>(content, pageable, result.getTotalElements()));
    }

    private Map<Long, Long> loadCommentCounts(List<Long> discussionIds) {
        Map<Long, Long> counts = new HashMap<>();
        if (discussionIds.isEmpty()) {
            return counts;
        }
        for (DiscussionCommentCount row : commentRepository.countByDiscussionIds(discussionIds)) {
            counts.put(row.getDiscussionId(), row.getTotal());
        }
        return counts;
    }

    private Set<Long> loadLikedIds(List<Long> discussionIds, UserDetail viewer) {
        if (viewer == null || discussionIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(discussionLikeRepository.findLikedDiscussionIds(viewer.getId(), discussionIds));
    }

    private DiscussionResponse toResponse(Discussion discussion, boolean liked) {
        int commentCount = (int) commentRepository.countByDiscussionId(discussion.getId());
        return discussionMapper.toResponse(discussion, commentCount, discussion.getLikesCount(), liked);
    }

    private Discussion findById(Long id) {
        return discussionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
    }

    private Discussion findActive(Long id) {
        Discussion discussion = findById(id);
        if (!Boolean.TRUE.equals(discussion.getIsActive())) {
            throw new ResourceNotFoundException(NOT_FOUND_MESSAGE);
        }
        return discussion;
    }

    private User loadUser(UserDetail actor) {
        return userRepository.findById(actor.getId())
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));
    }

    private void assertCanManage(Discussion discussion, UserDetail actor) {
        Long ownerId = discussion.getAuthor().getId();
        if (!actor.canManage(ownerId)) {
            throw new ForbiddenOperationException("Solo el autor o un administrador puede realizar esta acción");
        }
    }
}
