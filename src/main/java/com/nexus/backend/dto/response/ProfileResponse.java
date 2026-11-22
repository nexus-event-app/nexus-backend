package com.nexus.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

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
