package com.nexus.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nexus.backend.entity.Guide;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GuideResponse(Long id, String slug, String title, String description, String content, boolean published,
                            LocalDateTime publishedAt, LocalDateTime updatedAt) {

    public static GuideResponse full(Guide guide) {
        return new GuideResponse(guide.getId(), guide.getSlug(), guide.getTitle(), guide.getDescription(),
                guide.getContent(), guide.isPublished(), guide.getPublishedAt(), guide.getUpdatedAt());
    }

    public static GuideResponse summary(Guide guide) {
        return new GuideResponse(guide.getId(), guide.getSlug(), guide.getTitle(), guide.getDescription(), null,
                guide.isPublished(), guide.getPublishedAt(), guide.getUpdatedAt());
    }
}
