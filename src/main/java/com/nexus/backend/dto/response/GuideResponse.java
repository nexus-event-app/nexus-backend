package com.nexus.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nexus.backend.entity.Guide;
import com.nexus.backend.enums.GuideCategory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GuideResponse(Long id, String slug, String title, String description, String content, String language,
                            List<String> languages, GuideCategory category, boolean published,
                            LocalDateTime publishedAt, LocalDateTime updatedAt) {

    public static GuideResponse full(Guide guide, Locale locale) {
        return of(guide, locale, true);
    }

    public static GuideResponse summary(Guide guide, Locale locale) {
        return of(guide, locale, false);
    }

    private static GuideResponse of(Guide guide, Locale locale, boolean withContent) {
        String language = hasTranslation(guide, locale.getLanguage()) ? locale.getLanguage() : "es";
        String title = switch (language) {
            case "ca" -> guide.getTitleCa();
            case "en" -> guide.getTitleEn();
            default -> guide.getTitle();
        };
        String description = switch (language) {
            case "ca" -> guide.getDescriptionCa();
            case "en" -> guide.getDescriptionEn();
            default -> guide.getDescription();
        };
        String content = !withContent ? null : switch (language) {
            case "ca" -> guide.getContentCa();
            case "en" -> guide.getContentEn();
            default -> guide.getContent();
        };
        return new GuideResponse(guide.getId(), guide.getSlug(), title, description, content, language,
                languages(guide), guide.getCategory(), guide.isPublished(), guide.getPublishedAt(),
                guide.getUpdatedAt());
    }

    public static List<String> languages(Guide guide) {
        List<String> languages = new ArrayList<>(List.of("es"));
        if (hasTranslation(guide, "ca")) {
            languages.add("ca");
        }
        if (hasTranslation(guide, "en")) {
            languages.add("en");
        }
        return languages;
    }

    private static boolean hasTranslation(Guide guide, String language) {
        return switch (language) {
            case "ca" -> filled(guide.getTitleCa()) && filled(guide.getDescriptionCa()) && filled(guide.getContentCa());
            case "en" -> filled(guide.getTitleEn()) && filled(guide.getDescriptionEn()) && filled(guide.getContentEn());
            default -> false;
        };
    }

    private static boolean filled(String value) {
        return value != null && !value.isBlank();
    }
}
