package com.nexus.backend.dto.request;

import jakarta.validation.constraints.NotNull;

/** Comentario que quien abrió la discusión marca como la mejor respuesta. */
public record AcceptAnswerRequest(@NotNull(message = "{validation.commentId.required}") Long commentId) {
}
