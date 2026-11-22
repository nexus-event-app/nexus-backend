package com.nexus.backend.dto.response;

import com.nexus.backend.enums.NotificationType;

import java.time.LocalDateTime;

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
