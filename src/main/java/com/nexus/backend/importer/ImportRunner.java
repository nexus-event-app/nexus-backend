package com.nexus.backend.importer;

import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.entity.JobOffer;
import com.nexus.backend.entity.News;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.enums.WorkMode;
import com.nexus.backend.importer.provider.ImportedJob;
import com.nexus.backend.importer.provider.ImportedNews;
import com.nexus.backend.importer.provider.JobFeedProvider;
import com.nexus.backend.importer.provider.NewsFeedProvider;
import com.nexus.backend.repository.ImportExclusionRepository;
import com.nexus.backend.repository.ImportSourceRepository;
import com.nexus.backend.repository.JobOfferRepository;
import com.nexus.backend.repository.NewsRepository;
import com.nexus.backend.util.HtmlText;
import com.nexus.backend.util.ContentLanguage;
import com.nexus.backend.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Ejecuta una fuente: descarga, normaliza, filtra (antigüedad, nivel, sector, duplicados) y guarda.
 * La descarga se hace fuera de la transacción; el guardado y el estado de la fuente, dentro.
 */
@Slf4j
@Component
public class ImportRunner {

    private static final int MAX_TAGS = 10;
    private static final int MAX_TAG_LENGTH = 30;
    private static final int MAX_DESCRIPTION_LENGTH = 20000;

    private final List<NewsFeedProvider> newsProviders;
    private final List<JobFeedProvider> jobProviders;
    private final ImportSourceRepository sourceRepository;
    private final NewsRepository newsRepository;
    private final JobOfferRepository jobOfferRepository;
    private final ImportExclusionRepository exclusionRepository;
    private final TransactionTemplate transactionTemplate;
    private final ImportProperties properties;
    private final NewsImageBackfill newsImageBackfill;

    /** Fuentes en ejecución, para no lanzar la misma dos veces a la vez (planificador + admin). */
    private final Set<Long> running = ConcurrentHashMap.newKeySet();

    public ImportRunner(List<NewsFeedProvider> newsProviders,
                        List<JobFeedProvider> jobProviders,
                        ImportSourceRepository sourceRepository,
                        NewsRepository newsRepository,
                        JobOfferRepository jobOfferRepository,
                        ImportExclusionRepository exclusionRepository,
                        TransactionTemplate transactionTemplate,
                        ImportProperties properties,
                        NewsImageBackfill newsImageBackfill) {
        this.newsProviders = newsProviders;
        this.jobProviders = jobProviders;
        this.sourceRepository = sourceRepository;
        this.newsRepository = newsRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.exclusionRepository = exclusionRepository;
        this.newsImageBackfill = newsImageBackfill;
        this.transactionTemplate = transactionTemplate;
        this.properties = properties;
    }

    public boolean isRunning(Long sourceId) {
        return running.contains(sourceId);
    }

    public ImportRunResult run(ImportSource source) {
        if (!running.add(source.getId())) {
            return ImportRunResult.failure("Esta fuente ya se está importando");
        }
        ImportRunResult result;
        try {
            result = source.getType().isNews() ? runNews(source) : runJobs(source);
        } catch (ImportException e) {
            result = ImportRunResult.failure(e.getMessage());
            log.warn("Importación fallida de '{}': {}", source.getName(), e.getMessage());
        } catch (RuntimeException e) {
            result = ImportRunResult.failure("Error inesperado al importar");
            log.error("Error inesperado importando '{}'", source.getName(), e);
        } finally {
            running.remove(source.getId());
        }
        recordResult(source.getId(), result);
        return result;
    }

    private ImportRunResult runNews(ImportSource source) {
        NewsFeedProvider provider = newsProviders.stream()
                .filter(candidate -> candidate.supports(source.getType()))
                .findFirst()
                .orElseThrow(() -> new ImportException("Tipo de fuente no soportado"));
        List<ImportedNews> fetched = provider.fetch(source);

        LocalDateTime oldest = LocalDateTime.now().minusDays(properties.newsMaxAgeDays());
        KeywordFilter keywords = KeywordFilter.of(source.getKeywords());
        KeywordFilter excluded = KeywordFilter.exclusion(source.getExcludeKeywords());
        List<ImportedNews> candidates = fetched.stream()
                .filter(item -> keywords.matches(item.title() + " " + item.summary()))
                .filter(item -> !excluded.matches(item.title() + " " + item.summary()))
                .filter(item -> item.publishedAt() == null || item.publishedAt().isAfter(oldest))
                .filter(item -> item.link().length() <= 500)
                .sorted(Comparator.comparing(ImportedNews::publishedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        List<ImportedNews> fresh = withoutKnown(candidates, ImportedNews::externalId,
                newsRepository::findExistingExternalIds, properties.newsMaxItemsPerRun());

        // Se guardan de la más antigua a la más reciente para que el orden por fecha de alta se respete.
        List<News> entities = fresh.reversed().stream().map(item -> toNews(item, source)).toList();
        transactionTemplate.executeWithoutResult(status -> newsRepository.saveAll(entities));
        // Si el feed no trae imagen, se busca la portada en la página del artículo.
        if (!Boolean.FALSE.equals(source.getUseSourceImages())) {
            newsImageBackfill.fill(entities);
        }
        return ImportRunResult.success(fetched.size(), entities.size());
    }


    private ImportRunResult runJobs(ImportSource source) {
        JobFeedProvider provider = jobProviders.stream()
                .filter(candidate -> candidate.supports(source.getType()))
                .findFirst()
                .orElseThrow(() -> new ImportException("Tipo de fuente no soportado"));
        List<ImportedJob> fetched = provider.fetch(source);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oldest = now.minusDays(properties.jobMaxAgeDays());
        boolean remoteOnly = Boolean.TRUE.equals(source.getRemoteOnly());
        KeywordFilter keywords = KeywordFilter.of(source.getKeywords());
        KeywordFilter excluded = KeywordFilter.exclusion(source.getExcludeKeywords());
        List<ClassifiedJob> candidates = fetched.stream()
                .filter(job -> job.location() != null && job.applyUrl().length() <= 500)
                .filter(job -> job.publishedAt() == null || job.publishedAt().isAfter(oldest))
                .filter(job -> !remoteOnly || job.workMode() == WorkMode.REMOTE)
                .filter(job -> job.techSource() || JobClassifier.isTech(job.title(), job.tags()))
                .filter(job -> keywords.matches(job.title() + " " + String.join(" ", job.tags())))
                .filter(job -> !excluded.matches(job.title() + " " + String.join(" ", job.tags())))
                .map(job -> JobClassifier.level(job.title(), job.levelHints()).map(level -> new ClassifiedJob(job, level)))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing((ClassifiedJob c) -> c.job().publishedAt(),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        List<ClassifiedJob> fresh = withoutKnown(candidates, c -> c.job().externalId(),
                jobOfferRepository::findExistingExternalIds, properties.jobMaxItemsPerRun());

        List<JobOffer> entities = fresh.reversed().stream().map(c -> toJob(c, source, now)).toList();
        transactionTemplate.executeWithoutResult(status -> jobOfferRepository.saveAll(entities));
        return ImportRunResult.success(fetched.size(), entities.size());
    }

    /** Quita los ya importados, los borrados por un admin y los repetidos dentro del propio feed. */
    private <T> List<T> withoutKnown(List<T> items, Function<T, String> idOf,
                                     Function<Set<String>, Set<String>> existingLookup, int limit) {
        if (items.isEmpty()) {
            return List.of();
        }
        Set<String> ids = new HashSet<>(items.stream().map(idOf).toList());
        Set<String> known = new HashSet<>(existingLookup.apply(ids));
        known.addAll(exclusionRepository.findExisting(ids));
        return items.stream()
                .filter(item -> known.add(idOf.apply(item)))
                .limit(limit)
                .toList();
    }

    private News toNews(ImportedNews item, ImportSource source) {
        News news = new News();
        news.setTitle(TextUtil.truncate(item.title(), 200));
        news.setContent(item.summary().isEmpty() ? item.title() : item.summary());
        news.setSourceUrl(item.link());
        news.setLanguage(ContentLanguage.orDefault(source.getLanguage()));
        boolean useImage = !Boolean.FALSE.equals(source.getUseSourceImages()) && ImageUrlPolicy.isAcceptable(item.imageUrl());
        news.setImageUrl(useImage ? item.imageUrl() : null);
        news.setAuthor(TextUtil.truncate(Optional.ofNullable(item.author()).orElse(source.getName()), 100));
        news.setCategory(Optional.ofNullable(source.getNewsCategory()).orElse(NewsCategory.GENERAL));
        news.setIsActive(Boolean.TRUE.equals(source.getAutoPublish()));
        news.setSource(source.getName());
        news.setExternalId(item.externalId());
        return news;
    }

    private JobOffer toJob(ClassifiedJob classified, ImportSource source, LocalDateTime now) {
        ImportedJob item = classified.job();
        LocalDateTime publishedAt = item.publishedAt() == null || item.publishedAt().isAfter(now) ? now : item.publishedAt();
        String description = TextUtil.truncate(HtmlText.toPlainText(item.description()), MAX_DESCRIPTION_LENGTH);

        JobOffer job = new JobOffer();
        job.setTitle(TextUtil.truncate(item.title(), 200));
        job.setCompany(TextUtil.truncate(item.company(), 120));
        job.setDescription(description.isEmpty() ? item.title() : description);
        job.setLocation(TextUtil.truncate(item.location(), 120));
        job.setWorkMode(item.workMode());
        job.setContractType(item.contractType());
        job.setLevel(classified.level());
        job.setTags(joinTags(item.tags()));
        job.setApplyUrl(item.applyUrl());
        job.setSource(source.getName());
        job.setExternalId(item.externalId());
        job.setIsActive(Boolean.TRUE.equals(source.getAutoPublish()));
        job.setPublishedAt(publishedAt);
        job.setExpiresAt(publishedAt.plusDays(properties.jobTtlDays()));
        return job;
    }

    private static String joinTags(List<String> tags) {
        List<String> cleaned = tags.stream()
                .map(tag -> tag.replace(",", " ").trim())
                .filter(tag -> !tag.isEmpty() && tag.length() <= MAX_TAG_LENGTH)
                .distinct()
                .limit(MAX_TAGS)
                .toList();
        return cleaned.isEmpty() ? null : String.join(",", cleaned);
    }

    private void recordResult(Long sourceId, ImportRunResult result) {
        transactionTemplate.executeWithoutResult(status -> sourceRepository.findById(sourceId).ifPresent(source -> {
            source.setLastRunAt(LocalDateTime.now());
            source.setLastStatus(result.status());
            source.setLastMessage(TextUtil.truncate(result.message(), 500));
            source.setLastImportedCount(result.imported());
        }));
    }

    private record ClassifiedJob(ImportedJob job, JobLevel level) {
    }
}
