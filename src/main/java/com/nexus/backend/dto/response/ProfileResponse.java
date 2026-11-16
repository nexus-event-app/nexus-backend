package com.nexus.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Perfil de un usuario. No incluye nunca su email ni otros datos de la cuenta. Los contadores de actividad solo
 * llegan a su dueño (en el perfil público van a null y no se envían): por privacidad, nadie más ve su actividad.
 */
public record ProfileResponse(
        Long id,
        String slug,
        String name,
        String headline,
        String bio,
        List<String> technologies,
        String githubUrl,
        String linkedinUrl,
        String avatarUrl,
        LocalDateTime memberSince,
        @JsonInclude(JsonInclude.Include.NON_NULL) Long discussionCount,
        @JsonInclude(JsonInclude.Include.NON_NULL) Long testimonialCount) {
}
