package com.nexus.backend.importer;

import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.entity.JobOffer;
import com.nexus.backend.entity.News;
import com.nexus.backend.enums.ImportSourceType;
import com.nexus.backend.enums.ImportStatus;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.importer.provider.ProvidersTestFixtures;
import com.nexus.backend.repository.ImportExclusionRepository;
import com.nexus.backend.repository.ImportSourceRepository;
import com.nexus.backend.repository.JobOfferRepository;
import com.nexus.backend.repository.NewsRepository;
import com.nexus.backend.service.JobService;
import com.nexus.backend.service.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"app.import.news-max-age-days=36500", "app.import.job-max-age-days=36500"})
@ActiveProfiles("test")
class ImportRunnerIntegrationTest {

    @MockitoBean
    private FeedClient feedClient;

    @Autowired
    private ImportRunner importRunner;
    @Autowired
    private ImportSourceRepository sourceRepository;
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private JobOfferRepository jobOfferRepository;
    @Autowired
    private ImportExclusionRepository exclusionRepository;
    @Autowired
    private NewsService newsService;
    @Autowired
    private JobService jobService;

    @BeforeEach
    void cleanDatabase() {
        newsRepository.deleteAll();
        jobOfferRepository.deleteAll();
        exclusionRepository.deleteAll();
        sourceRepository.deleteAll();
    }

    @Test
    void importsNewsOnceAndRespectsDeletedItems() throws Exception {
        ImportSource source = source("freeCodeCamp", ImportSourceType.RSS_NEWS, "https://feeds.example.com/fcc", false);
        when(feedClient.get(eq(source.getUrl()), anyString())).thenReturn(ProvidersTestFixtures.bytes("freecodecamp.xml"));

        ImportRunResult first = importRunner.run(source);
        assertThat(first.status()).isEqualTo(ImportStatus.SUCCESS);
        assertThat(first.imported()).isEqualTo(3);

        List<News> saved = newsRepository.findAll();
        assertThat(saved).allSatisfy(news -> {
            assertThat(news.getSource()).isEqualTo("freeCodeCamp");
            assertThat(news.getCategory()).isEqualTo(NewsCategory.RECURSOS);
            assertThat(news.getIsActive()).isTrue();
            assertThat(news.getExternalId()).startsWith("rss:");
        });

        assertThat(importRunner.run(source).imported()).as("segunda ejecución sin duplicados").isZero();

        newsService.deleteNews(saved.getFirst().getId());
        assertThat(importRunner.run(source).imported()).as("lo borrado no vuelve").isZero();
        assertThat(newsRepository.count()).isEqualTo(2);

        ImportSource updated = sourceRepository.findById(source.getId()).orElseThrow();
        assertThat(updated.getLastStatus()).isEqualTo(ImportStatus.SUCCESS);
        assertThat(updated.getLastRunAt()).isNotNull();
    }

    @Test
    void importsOnlyJuniorTechJobsAndHonoursRemoteOnly() throws Exception {
        ImportSource arbeitnow = source("Arbeitnow", ImportSourceType.ARBEITNOW_JOBS, "https://api.example.com/arbeitnow", false);
        when(feedClient.get(eq(arbeitnow.getUrl()), anyString())).thenReturn(ProvidersTestFixtures.bytes("arbeitnow.json"));

        ImportRunResult result = importRunner.run(arbeitnow);

        assertThat(result.fetched()).isEqualTo(17);
        List<JobOffer> jobs = jobOfferRepository.findAll();
        assertThat(jobs).extracting(JobOffer::getTitle).containsExactlyInAnyOrder(
                "Junior Fachinformatiker/-in für Anwendungsentwicklung – Vollzeit (m/w/d)",
                "Frontend Developer (React)");
        assertThat(jobs).allSatisfy(job -> {
            assertThat(job.getLevel()).isEqualTo(JobLevel.JUNIOR);
            assertThat(job.getSource()).isEqualTo("Arbeitnow");
            assertThat(job.getExpiresAt()).isEqualTo(job.getPublishedAt().plusDays(30));
            assertThat(job.getDescription()).doesNotContain("<");
        });

        jobService.deleteJob(jobs.getFirst().getId());
        jobOfferRepository.deleteAll();
        arbeitnow.setRemoteOnly(true);
        sourceRepository.save(arbeitnow);
        exclusionRepository.deleteAll();
        importRunner.run(arbeitnow);
        assertThat(jobOfferRepository.findAll()).extracting(JobOffer::getTitle).containsExactly("Frontend Developer (React)");
    }

    @Test
    void importsRemotiveJuniorJobsAsUnpublishedWhenAutoPublishIsOff() throws Exception {
        ImportSource remotive = source("Remotive", ImportSourceType.REMOTIVE_JOBS, "https://api.example.com/remotive", false);
        remotive.setAutoPublish(false);
        sourceRepository.save(remotive);
        when(feedClient.get(eq(remotive.getUrl()), anyString())).thenReturn(ProvidersTestFixtures.bytes("remotive.json"));

        importRunner.run(remotive);

        List<JobOffer> jobs = jobOfferRepository.findAll();
        assertThat(jobs).extracting(JobOffer::getTitle).contains("Junior Frontend Developer (React)");
        assertThat(jobs).allSatisfy(job -> assertThat(job.getIsActive()).isFalse());
    }

    @Test
    void importsGetonbrdJuniorAndInternshipJobsOnly() throws Exception {
        ImportSource getonbrd = source("Get on Board", ImportSourceType.GETONBRD_JOBS, "https://api.example.com/gob/jobs", false);
        when(feedClient.get(anyString(), anyString())).thenReturn(ProvidersTestFixtures.bytes("getonbrd.json"));

        importRunner.run(getonbrd);

        List<JobOffer> jobs = jobOfferRepository.findAll();
        assertThat(jobs).hasSize(5);
        assertThat(jobs).extracting(JobOffer::getTitle).doesNotContain("Líder Técnico", "Founding GTM Engineer")
                .noneMatch(title -> title.startsWith("Semi Senior"));
        assertThat(jobs).filteredOn(job -> job.getTitle().startsWith("Práctica"))
                .singleElement().extracting(JobOffer::getLevel).isEqualTo(JobLevel.INTERN);
    }

    @Test
    void excludedKeywordsSkipMatchingItems() throws Exception {
        ImportSource source = source("Con exclusiones", ImportSourceType.RSS_NEWS, "https://feeds.example.com/excl", false);
        source.setExcludeKeywords("escalabilidad");
        sourceRepository.save(source);
        when(feedClient.get(eq(source.getUrl()), anyString())).thenReturn(ProvidersTestFixtures.bytes("freecodecamp.xml"));

        assertThat(importRunner.run(source).imported()).isEqualTo(2);
        assertThat(newsRepository.findAll()).extracting(News::getTitle)
                .noneMatch(title -> title.toLowerCase().contains("escalabilidad"));
    }

    @Test
    void recordsFailuresWithoutThrowing() {
        ImportSource source = source("Roto", ImportSourceType.RSS_NEWS, "https://feeds.example.com/roto", false);
        when(feedClient.get(eq(source.getUrl()), anyString())).thenThrow(new ImportException("La fuente respondió con el estado HTTP 503"));

        ImportRunResult result = importRunner.run(source);

        assertThat(result.status()).isEqualTo(ImportStatus.FAILED);
        ImportSource updated = sourceRepository.findById(source.getId()).orElseThrow();
        assertThat(updated.getLastStatus()).isEqualTo(ImportStatus.FAILED);
        assertThat(updated.getLastMessage()).contains("503");
    }

    private ImportSource source(String name, ImportSourceType type, String url, boolean remoteOnly) {
        ImportSource source = new ImportSource();
        source.setName(name);
        source.setType(type);
        source.setUrl(url);
        source.setNewsCategory(type.isNews() ? NewsCategory.RECURSOS : null);
        source.setRemoteOnly(remoteOnly);
        source.setIntervalMinutes(type.getMinIntervalMinutes());
        return sourceRepository.save(source);
    }
}
