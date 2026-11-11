package com.nexus.backend.dto.response;

import com.nexus.backend.enums.NotificationType;

import java.time.LocalDateTime;

/** Aviso tal como lo ve su destinatario: qué ha pasado, quién lo ha hecho y dónde. */
public record NotificationResponse(
        Long id,
        NotificationType type,
        Long actorId,
        String actorName,
        String actorAvatarUrl,
        Long discussionId,
        String discussionSlug,
        String discussionTitle,
        Long commentId,
        boolean read,
        LocalDateTime createdAt) {
}
