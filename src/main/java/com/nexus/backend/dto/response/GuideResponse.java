package com.nexus.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nexus.backend.entity.Guide;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GuideResponse(Long id, String slug, String title, String description, String content, boolean published,
                            int readingMinutes, LocalDateTime publishedAt, LocalDateTime updatedAt) {

    private static final int WORDS_PER_MINUTE = 200;

    public static GuideResponse full(Guide guide) {
        return of(guide, guide.getContent());
    }

    public static GuideResponse summary(Guide guide) {
        return of(guide, null);
    }

    private static GuideResponse of(Guide guide, String content) {
        return new GuideResponse(guide.getId(), guide.getSlug(), guide.getTitle(), guide.getDescription(), content,
                guide.isPublished(), readingMinutes(guide.getContent()), guide.getPublishedAt(), guide.getUpdatedAt());
    }

    static int readingMinutes(String markdown) {
        String text = markdown == null ? "" : markdown.replaceAll("\\[([^\\]]+)]\\([^)]+\\)", "$1").replaceAll("[#*]", " ");
        long words = text.isBlank() ? 0 : text.trim().split("\\s+").length;
        return (int) Math.max(1, (words + WORDS_PER_MINUTE - 1) / WORDS_PER_MINUTE);
    }
}
