package com.nexus.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nexus.backend.entity.Guide;
import com.nexus.backend.enums.GuideCategory;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GuideResponse(Long id, String slug, String title, String description, String content,
                            GuideCategory category, boolean published, LocalDateTime publishedAt,
                            LocalDateTime updatedAt) {

    public static GuideResponse full(Guide guide) {
        return of(guide, guide.getContent());
    }

    public static GuideResponse summary(Guide guide) {
        return of(guide, null);
    }

    private static GuideResponse of(Guide guide, String content) {
        return new GuideResponse(guide.getId(), guide.getSlug(), guide.getTitle(), guide.getDescription(), content,
                guide.getCategory(), guide.isPublished(), guide.getPublishedAt(), guide.getUpdatedAt());
    }
}
