package com.nexus.backend.importer.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.WorkMode;
import com.nexus.backend.importer.ImportException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Prueba los proveedores con recortes de respuestas reales (src/test/resources/feeds). */
class ProvidersTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RssNewsProvider rss = new RssNewsProvider(null);
    private final RemotiveJobProvider remotive = new RemotiveJobProvider(null, objectMapper);
    private final ArbeitnowJobProvider arbeitnow = new ArbeitnowJobProvider(null, objectMapper);
    private final GetonbrdJobProvider getonbrd = new GetonbrdJobProvider(null, objectMapper);

    @Test
    void parsesRealRssFeedWithMediaImages() throws IOException {
        List<ImportedNews> items = rss.parse(fixture("freecodecamp.xml"));

        assertThat(items).hasSize(3);
        ImportedNews first = items.getFirst();
        assertThat(first.title()).isEqualTo("Escalabilidad en el desarrollo de aplicaciones");
        assertThat(first.link()).startsWith("https://www.freecodecamp.org/espanol/news/");
        assertThat(first.imageUrl()).startsWith("https://www.freecodecamp.org/espanol/news/content/images/");
        assertThat(first.summary()).isNotBlank().doesNotContain("<").hasSizeLessThanOrEqualTo(RssNewsProvider.SUMMARY_MAX_LENGTH);
        assertThat(first.externalId()).startsWith("rss:").hasSize(68);
        assertThat(first.publishedAt()).isNotNull();
        assertThat(items).extracting(ImportedNews::externalId).doesNotHaveDuplicates();
    }

    @Test
    void parsesAtomAndSkipsEntriesWithoutLink() throws IOException {
        List<ImportedNews> items = rss.parse(fixture("atom.xml"));

        assertThat(items).hasSize(1);
        ImportedNews entry = items.getFirst();
        assertThat(entry.title()).isEqualTo("Novedades de Java 25 & Spring");
        assertThat(entry.summary()).isEqualTo("Primer párrafo con enlace.\n\nSegundo párrafo.");
        assertThat(entry.imageUrl()).isEqualTo("https://blog.example.com/img.png");
        assertThat(entry.author()).isEqualTo("Ana López");
    }

    @Test
    void rejectsInvalidFeedsAndDoctypes() {
        assertThatThrownBy(() -> rss.parse("<html>no</html>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(ImportException.class);
        String xxe = "<?xml version=\"1.0\"?><!DOCTYPE rss [<!ENTITY x SYSTEM \"file:///etc/passwd\">]>"
                + "<rss version=\"2.0\"><channel><title>&x;</title></channel></rss>";
        assertThatThrownBy(() -> rss.parse(xxe.getBytes(StandardCharsets.UTF_8))).isInstanceOf(ImportException.class);
    }

    @Test
    void parsesRemotiveJobs() throws IOException {
        List<ImportedJob> jobs = remotive.parse(objectMapper.readTree(fixture("remotive.json")));

        assertThat(jobs).hasSize(5).allSatisfy(job -> {
            assertThat(job.workMode()).isEqualTo(WorkMode.REMOTE);
            assertThat(job.techSource()).isTrue();
            assertThat(job.applyUrl()).startsWith("https://remotive.com/");
        });
        ImportedJob junior = jobs.getLast();
        assertThat(junior.externalId()).isEqualTo("remotive:999001");
        assertThat(junior.location()).isEqualTo("Remoto (Europe)");
        assertThat(junior.contractType()).isEqualTo(ContractType.FULL_TIME);
        assertThat(junior.tags()).containsExactly("react", "javascript");
    }

    @Test
    void parsesArbeitnowJobs() throws IOException {
        List<ImportedJob> jobs = arbeitnow.parse(objectMapper.readTree(fixture("arbeitnow.json")));

        assertThat(jobs).hasSize(17);
        ImportedJob remote = jobs.getLast();
        assertThat(remote.externalId()).isEqualTo("arbeitnow:frontend-developer-remote-entry-2");
        assertThat(remote.workMode()).isEqualTo(WorkMode.REMOTE);
        assertThat(remote.location()).isEqualTo("Remoto (Hamburg)");
        assertThat(remote.levelHints()).contains("entry");
        assertThat(remote.publishedAt()).isNotNull();
        assertThat(jobs).filteredOn(job -> job.title().startsWith("Praktikum"))
                .allSatisfy(job -> assertThat(job.contractType()).isEqualTo(ContractType.INTERNSHIP));
    }

    @Test
    void parsesGetonbrdJobsWithLevelHints() throws IOException {
        List<ImportedJob> jobs = getonbrd.parse(objectMapper.readTree(fixture("getonbrd.json")));

        assertThat(jobs).hasSize(8).allSatisfy(job -> {
            assertThat(job.externalId()).startsWith("getonbrd:");
            assertThat(job.applyUrl()).startsWith("https://www.getonbrd.com/jobs/");
            assertThat(job.company()).isNotBlank();
            assertThat(job.techSource()).isTrue();
        });
        ImportedJob internship = jobs.getFirst();
        assertThat(internship.title()).isEqualTo("Práctica Profesional – Ingeniería");
        assertThat(internship.contractType()).isEqualTo(ContractType.INTERNSHIP);
        assertThat(internship.levelHints()).containsExactly("junior", "internship");
        assertThat(internship.location()).isEqualTo("Chile");
        assertThat(jobs.get(1).workMode()).isEqualTo(WorkMode.HYBRID);
        assertThat(jobs.get(1).tags()).isNotEmpty();
        assertThat(jobs.getLast().workMode()).isEqualTo(WorkMode.REMOTE);
        assertThat(jobs.getLast().levelHints()).containsExactly("senior");
    }

    @Test
    void buildsGetonbrdPageUrls() {
        assertThat(GetonbrdJobProvider.pageUrl("https://www.getonbrd.com/api/v0/categories/programming/jobs?per_page=100", 2))
                .isEqualTo("https://www.getonbrd.com/api/v0/categories/programming/jobs?per_page=100"
                        + "&expand=%5B%22company%22%2C%22tags%22%5D&page=2");
        assertThat(GetonbrdJobProvider.pageUrl("https://x.test/jobs", 1))
                .isEqualTo("https://x.test/jobs?expand=%5B%22company%22%2C%22tags%22%5D&page=1");
    }

    static byte[] fixture(String name) throws IOException {
        try (InputStream in = ProvidersTest.class.getResourceAsStream("/feeds/" + name)) {
            return in.readAllBytes();
        }
    }
}
