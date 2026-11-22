package com.nexus.backend.service;

import com.nexus.backend.dto.response.NotificationResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.entity.Comment;
import com.nexus.backend.security.UserDetail;

public interface NotificationService {

    PageResponse<NotificationResponse> getNotifications(int page, int size, UserDetail actor);

    long countUnread(UserDetail actor);

    void markRead(Long id, UserDetail actor);

    void markAllRead(UserDetail actor);

    void notifyNewComment(Comment comment);

    void notifyAcceptedAnswer(Comment comment);

    void withdrawAcceptedAnswer(Long commentId);
}
