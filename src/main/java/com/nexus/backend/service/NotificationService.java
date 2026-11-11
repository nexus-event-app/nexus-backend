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

    /** Avisa a quien abrió la discusión de que tiene una respuesta nueva (salvo que se responda a sí mismo). */
    void notifyNewComment(Comment comment);

    /** Avisa a quien escribió el comentario de que lo han marcado como la mejor respuesta. */
    void notifyAcceptedAnswer(Comment comment);

    /** Al desmarcar la mejor respuesta, su aviso deja de ser cierto y se borra. */
    void withdrawAcceptedAnswer(Long commentId);
}
