package com.nexus.backend.seo;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nexus.backend.i18n.Languages;
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
import java.util.Locale;

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
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:xhtml=\"http://www.w3.org/1999/xhtml\">\n");

        for (String path : STATIC_PATHS) {
            appendUrl(xml, path, null);
        }
        appendEntries(xml, "/noticias/", newsRepository.findSitemapEntries(limit));
        appendEntries(xml, "/empleo/", jobOfferRepository.findSitemapEntries(LocalDateTime.now(), limit));
        appendEntries(xml, "/foro/", discussionRepository.findSitemapEntries(limit));

        xml.append("</urlset>\n");
        return xml.toString();
    }

    private void appendEntries(StringBuilder xml, String section, List<SitemapEntry> entries) {
        for (SitemapEntry entry : entries) {
            appendUrl(xml, section + SlugUtil.idSlug(entry.getId(), entry.getTitle()), entry.getLastModified());
        }
    }

    /** Una entrada por idioma, cada una con los enlaces alternativos a todas las versiones (hreflang). */
    private void appendUrl(StringBuilder xml, String path, LocalDateTime lastModified) {
        for (Locale locale : Languages.SUPPORTED) {
            xml.append("  <url>\n");
            xml.append("    <loc>").append(escapeXml(localizedUrl(locale, path))).append("</loc>\n");
            if (lastModified != null) {
                xml.append("    <lastmod>").append(DateTimeFormatter.ISO_LOCAL_DATE.format(lastModified)).append("</lastmod>\n");
            }
            for (Locale alternate : Languages.SUPPORTED) {
                xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"").append(alternate.getLanguage())
                        .append("\" href=\"").append(escapeXml(localizedUrl(alternate, path))).append("\"/>\n");
            }
            xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"x-default\" href=\"")
                    .append(escapeXml(localizedUrl(Languages.DEFAULT, path))).append("\"/>\n");
            xml.append("  </url>\n");
        }
    }

    private String localizedUrl(Locale locale, String path) {
        String prefix = Languages.pathPrefix(locale);
        // La portada en castellano es la raíz; en otros idiomas, «/ca» y «/en» sin barra final.
        if ("/".equals(path)) {
            return prefix.isEmpty() ? publicUrl + "/" : publicUrl + prefix;
        }
        return publicUrl + prefix + path;
    }

    private String buildRobots() {
        StringBuilder robots = new StringBuilder("User-agent: *\nAllow: /\nDisallow: /api/\nDisallow: /admin\n");
        for (Locale locale : Languages.SUPPORTED) {
            String prefix = Languages.pathPrefix(locale);
            robots.append("Disallow: ").append(prefix).append("/login\n");
            robots.append("Disallow: ").append(prefix).append("/registro\n");
        }
        return robots.append("\nSitemap: ").append(publicUrl).append("/sitemap.xml\n").toString();
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
