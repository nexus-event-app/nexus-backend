package com.nexus.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record AcceptAnswerRequest(@NotNull(message = "{validation.commentId.required}") Long commentId) {
}
