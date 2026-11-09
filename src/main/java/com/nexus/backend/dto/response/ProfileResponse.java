package com.nexus.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Perfil público de un usuario. No incluye nunca su email ni otros datos de la cuenta. Lo usan tanto la página
 * pública como el formulario «Editar perfil» (que parte de los mismos datos).
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
        long discussionCount,
        long testimonialCount) {
}
