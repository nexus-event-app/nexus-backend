package com.nexus.backend.dto.response;

import java.time.LocalDateTime;

/** Etapa hecha de una ruta de aprendizaje. */
public record LearningProgressResponse(String pathId, String stepId, LocalDateTime completedAt) {
}
