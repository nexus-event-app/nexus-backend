package com.nexus.backend.importer.provider;

import java.time.LocalDateTime;

/** Noticia normalizada a partir de una fuente externa, antes de filtrarla y guardarla. */
public record ImportedNews(
        String externalId,
        String title,
        String summary,
        String link,
        String imageUrl,
        String author,
        LocalDateTime publishedAt) {
}
