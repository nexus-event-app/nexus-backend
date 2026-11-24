package com.nexus.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nexus.backend.entity.Guide;
import com.nexus.backend.enums.GuideCategory;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminGuideResponse(Long id, String slug, String title, String description, String content,
                                 String titleCa, String descriptionCa, String contentCa,
                                 String titleEn, String descriptionEn, String contentEn,
                                 List<String> languages, GuideCategory category, boolean published,
                                 LocalDateTime publishedAt, LocalDateTime updatedAt) {

    public static AdminGuideResponse full(Guide guide) {
        return new AdminGuideResponse(guide.getId(), guide.getSlug(), guide.getTitle(), guide.getDescription(),
                guide.getContent(), guide.getTitleCa(), guide.getDescriptionCa(), guide.getContentCa(),
                guide.getTitleEn(), guide.getDescriptionEn(), guide.getContentEn(), GuideResponse.languages(guide),
                guide.getCategory(), guide.isPublished(), guide.getPublishedAt(), guide.getUpdatedAt());
    }

    public static AdminGuideResponse summary(Guide guide) {
        return new AdminGuideResponse(guide.getId(), guide.getSlug(), guide.getTitle(), guide.getDescription(), null,
                null, null, null, null, null, null, GuideResponse.languages(guide), guide.getCategory(),
                guide.isPublished(), guide.getPublishedAt(), guide.getUpdatedAt());
    }
}
