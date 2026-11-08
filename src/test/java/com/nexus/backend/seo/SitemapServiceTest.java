package com.nexus.backend.seo;

import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.JobOfferRepository;
import com.nexus.backend.repository.NewsRepository;
import com.nexus.backend.repository.projection.SitemapEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * El sitemap solo declara versiones por idioma (hreflang) de las páginas realmente traducidas. Noticias,
 * ofertas, debates y páginas legales solo existen en castellano: una URL, sin alternativas.
 */
class SitemapServiceTest {

    private static final String SITE = "https://primercommit.test";
    private static final Pattern URL_BLOCK = Pattern.compile("<url>(.*?)</url>", Pattern.DOTALL);

    private String xml;

    @BeforeEach
    void buildSitemap() {
        NewsRepository news = mock(NewsRepository.class);
        JobOfferRepository jobs = mock(JobOfferRepository.class);
        DiscussionRepository discussions = mock(DiscussionRepository.class);
        when(news.findSitemapEntries(any())).thenReturn(List.of(entry(7L, "Hola mundo")));
        when(jobs.findSitemapEntries(any(), any())).thenReturn(List.of(entry(3L, "Junior Java")));
        when(discussions.findSitemapEntries(any())).thenReturn(List.of(entry(5L, "Mi duda")));
        xml = new SitemapService(news, jobs, discussions, SITE + "/").getSitemapXml();
    }

    @Test
    void translatedPagesAreListedInEveryLanguageWithHreflang() {
        assertThat(block(SITE + "/")).contains("hreflang=\"ca\" href=\"" + SITE + "/ca\"",
                "hreflang=\"en\" href=\"" + SITE + "/en\"", "hreflang=\"x-default\" href=\"" + SITE + "/\"");
        assertThat(block(SITE + "/en/empleo")).contains("hreflang=\"es\" href=\"" + SITE + "/empleo\"");
        assertThat(block(SITE + "/ca/foro")).isNotNull();
    }

    @Test
    void spanishOnlyContentIsListedOnceWithoutAlternates() {
        for (String path : List.of("/noticias/7-hola-mundo", "/empleo/3-junior-java", "/foro/5-mi-duda", "/aviso-legal",
                "/privacidad", "/cookies")) {
            assertThat(block(SITE + path)).as(path).doesNotContain("hreflang");
            assertThat(xml).as(path).doesNotContain("<loc>" + SITE + "/ca" + path + "</loc>")
                    .doesNotContain("<loc>" + SITE + "/en" + path + "</loc>");
        }
        assertThat(block(SITE + "/noticias/7-hola-mundo")).contains("<lastmod>2026-11-01</lastmod>");
    }

    /** El bloque <url> cuya <loc> es exactamente la indicada. */
    private String block(String loc) {
        Matcher matcher = URL_BLOCK.matcher(xml);
        while (matcher.find()) {
            if (matcher.group(1).contains("<loc>" + loc + "</loc>")) {
                return matcher.group(1);
            }
        }
        throw new AssertionError("El sitemap no contiene " + loc);
    }

    private static SitemapEntry entry(Long id, String title) {
        return new SitemapEntry() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public LocalDateTime getLastModified() {
                return LocalDateTime.of(2026, 11, 1, 10, 0);
            }
        };
    }
}
