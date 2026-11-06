package com.nexus.backend.seo;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.JobOfferRepository;
import com.nexus.backend.repository.NewsRepository;
import com.nexus.backend.repository.projection.SitemapEntry;
import com.nexus.backend.util.SlugUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Genera sitemap.xml y robots.txt, con caché en memoria para que los bots no carguen la base de datos. */
@Service
public class SitemapService {

    private static final int MAX_URLS_PER_TYPE = 5000;
    private static final String CACHE_KEY = "content";
    private static final List<String> STATIC_PATHS = List.of("/", "/noticias", "/empleo", "/foro", "/testimonios");

    private final NewsRepository newsRepository;
    private final JobOfferRepository jobOfferRepository;
    private final DiscussionRepository discussionRepository;
    private final String publicUrl;

    private final Cache<String, String> sitemapCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(1)
            .build();

    private final Cache<String, String> robotsCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(1)
            .build();

    public SitemapService(NewsRepository newsRepository,
                          JobOfferRepository jobOfferRepository,
                          DiscussionRepository discussionRepository,
                          @Value("${app.public-url:http://localhost:5173}") String publicUrl) {
        this.newsRepository = newsRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.discussionRepository = discussionRepository;
        this.publicUrl = stripTrailingSlash(publicUrl);
    }

    public String getSitemapXml() {
        return sitemapCache.get(CACHE_KEY, key -> buildSitemap());
    }

    public String getRobotsTxt() {
        return robotsCache.get(CACHE_KEY, key -> buildRobots());
    }

    private String buildSitemap() {
        Pageable limit = PageRequest.of(0, MAX_URLS_PER_TYPE);
        StringBuilder xml = new StringBuilder(4096);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        for (String path : STATIC_PATHS) {
            appendUrl(xml, publicUrl + path, null);
        }
        appendEntries(xml, "/noticias/", newsRepository.findSitemapEntries(limit));
        appendEntries(xml, "/empleo/", jobOfferRepository.findSitemapEntries(LocalDateTime.now(), limit));
        appendEntries(xml, "/foro/", discussionRepository.findSitemapEntries(limit));

        xml.append("</urlset>\n");
        return xml.toString();
    }

    private void appendEntries(StringBuilder xml, String section, List<SitemapEntry> entries) {
        for (SitemapEntry entry : entries) {
            String location = publicUrl + section + SlugUtil.idSlug(entry.getId(), entry.getTitle());
            appendUrl(xml, location, entry.getLastModified());
        }
    }

    private void appendUrl(StringBuilder xml, String location, LocalDateTime lastModified) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapeXml(location)).append("</loc>\n");
        if (lastModified != null) {
            xml.append("    <lastmod>").append(DateTimeFormatter.ISO_LOCAL_DATE.format(lastModified)).append("</lastmod>\n");
        }
        xml.append("  </url>\n");
    }

    private String buildRobots() {
        return "User-agent: *\n"
                + "Allow: /\n"
                + "Disallow: /api/\n"
                + "Disallow: /admin\n"
                + "Disallow: /login\n"
                + "Disallow: /registro\n"
                + "\n"
                + "Sitemap: " + publicUrl + "/sitemap.xml\n";
    }

    private static String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static String stripTrailingSlash(String url) {
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
