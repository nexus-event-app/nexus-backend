package com.nexus.backend.dto.response;

import java.time.LocalDateTime;

public record LearningProgressResponse(String pathId, String stepId, LocalDateTime completedAt) {
}
