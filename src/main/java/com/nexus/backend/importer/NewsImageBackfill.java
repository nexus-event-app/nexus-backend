package com.nexus.backend.importer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nexus.backend.entity.News;
import com.nexus.backend.repository.NewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Busca la imagen de portada de las noticias que no la tienen, en la página del artículo ({@link ArticleImageFinder}).
 * Vale para todas: importadas (también de fuentes ya pausadas) y creadas a mano con enlace a la fuente. Se ejecuta
 * en cada revisión del planificador con un lote pequeño, y no reintenta la misma noticia hasta pasadas unas horas.
 */
@Slf4j
@Component
public class NewsImageBackfill {

    static final int LOOKBACK_DAYS = 30;
    static final int BATCH_SIZE = 20;
    private static final Duration RETRY_AFTER = Duration.ofHours(12);

    private final NewsRepository newsRepository;
    private final ArticleImageFinder articleImageFinder;
    private final TransactionTemplate transactionTemplate;
    private final Cache<Long, Boolean> recentlyTried = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(RETRY_AFTER)
            .build();

    public NewsImageBackfill(NewsRepository newsRepository, ArticleImageFinder articleImageFinder,
                             TransactionTemplate transactionTemplate) {
        this.newsRepository = newsRepository;
        this.articleImageFinder = articleImageFinder;
        this.transactionTemplate = transactionTemplate;
    }

    /** Recién importadas: se buscan en el momento. */
    public void fill(List<News> news) {
        news.stream().filter(item -> item.getImageUrl() == null && item.getSourceUrl() != null).forEach(this::tryFill);
    }

    /** Un lote de noticias de los últimos días sin imagen. Devuelve cuántas se han completado. */
    public int run() {
        LocalDateTime since = LocalDateTime.now().minusDays(LOOKBACK_DAYS);
        List<News> pending = newsRepository.findWithoutImage(since, PageRequest.of(0, BATCH_SIZE * 5)).stream()
                .filter(item -> recentlyTried.getIfPresent(item.getId()) == null)
                .limit(BATCH_SIZE)
                .toList();
        int filled = 0;
        for (News item : pending) {
            recentlyTried.put(item.getId(), Boolean.TRUE);
            if (tryFill(item)) {
                filled++;
            }
        }
        if (filled > 0) {
            log.info("Imagen de portada añadida a {} noticias", filled);
        }
        return filled;
    }

    private boolean tryFill(News item) {
        return articleImageFinder.find(item.getSourceUrl())
                .map(url -> {
                    Integer updated = transactionTemplate.execute(status -> newsRepository.updateImageUrl(item.getId(), url));
                    return updated != null && updated > 0;
                })
                .orElse(false);
    }
}
