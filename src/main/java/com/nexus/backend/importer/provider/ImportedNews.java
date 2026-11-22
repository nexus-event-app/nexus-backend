package com.nexus.backend.importer.provider;

import java.time.LocalDateTime;

public record ImportedNews(
        String externalId,
        String title,
        String summary,
        String link,
        String imageUrl,
        String author,
        LocalDateTime publishedAt) {
}
