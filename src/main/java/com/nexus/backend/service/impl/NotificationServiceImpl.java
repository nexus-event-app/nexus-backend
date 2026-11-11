package com.nexus.backend.service.impl;

import com.nexus.backend.dto.response.NotificationResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.entity.Comment;
import com.nexus.backend.entity.Discussion;
import com.nexus.backend.entity.Notification;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.NotificationType;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.repository.NotificationRepository;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.NotificationService;
import com.nexus.backend.util.Pagination;
import com.nexus.backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(int page, int size, UserDetail actor) {
        return PageResponse.from(notificationRepository
                .findVisible(actor.getId(), Pagination.newestFirst(page, size))
                .map(NotificationServiceImpl::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(UserDetail actor) {
        return notificationRepository.countUnread(actor.getId());
    }

    @Override
    @Transactional
    public void markRead(Long id, UserDetail actor) {
        // Solo se encuentran los avisos propios: los de otra persona responden igual que si no existieran.
        Notification notification = notificationRepository.findByIdAndRecipientId(id, actor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("error.notification.notFound"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public void markAllRead(UserDetail actor) {
        notificationRepository.markAllRead(actor.getId(), LocalDateTime.now());
    }

    @Override
    @Transactional
    public void notifyNewComment(Comment comment) {
        Discussion discussion = comment.getDiscussion();
        create(NotificationType.COMMENT, discussion.getAuthor(), comment.getAuthor(), discussion, comment);
    }

    @Override
    @Transactional
    public void notifyAcceptedAnswer(Comment comment) {
        Discussion discussion = comment.getDiscussion();
        create(NotificationType.ACCEPTED_ANSWER, comment.getAuthor(), discussion.getAuthor(), discussion, comment);
    }

    @Override
    @Transactional
    public void withdrawAcceptedAnswer(Long commentId) {
        notificationRepository.deleteByTypeAndCommentId(NotificationType.ACCEPTED_ANSWER, commentId);
    }

    private void create(NotificationType type, User recipient, User actor, Discussion discussion, Comment comment) {
        // Nadie recibe avisos de lo que hace él mismo.
        if (recipient.getId().equals(actor.getId())) {
            return;
        }
        Notification notification = new Notification();
        notification.setType(type);
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setDiscussion(discussion);
        notification.setComment(comment);
        notificationRepository.save(notification);
    }

    private static NotificationResponse toResponse(Notification notification) {
        User actor = notification.getActor();
        Discussion discussion = notification.getDiscussion();
        Comment comment = notification.getComment();
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                actor.getId(),
                actor.getName(),
                actor.getAvatarUrl(),
                discussion.getId(),
                SlugUtil.slugify(discussion.getTitle()),
                discussion.getTitle(),
                comment == null ? null : comment.getId(),
                notification.getReadAt() != null,
                notification.getCreatedAt());
    }
}
