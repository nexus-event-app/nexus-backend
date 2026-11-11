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
    /** Páginas con la interfaz y el contenido traducidos: se publican en todos los idiomas con hreflang. */
    private static final List<String> TRANSLATED_PATHS = List.of("/", "/noticias", "/empleo", "/foro", "/testimonios",
            "/sobre-el-proyecto", "/rutas", "/rutas/frontend", "/rutas/backend", "/rutas/qa", "/rutas/datos", "/rutas/ia");
    /** Páginas cuyo texto solo existe en castellano: una única URL, sin alternativas de idioma. */
    private static final List<String> SPANISH_ONLY_PATHS = List.of("/aviso-legal", "/privacidad", "/cookies");

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

        for (String path : TRANSLATED_PATHS) {
            appendTranslatedUrl(xml, path);
        }
        for (String path : SPANISH_ONLY_PATHS) {
            appendSingleUrl(xml, path, null);
        }
        appendEntries(xml, "/noticias/", newsRepository.findSitemapEntries(limit));
        appendEntries(xml, "/empleo/", jobOfferRepository.findSitemapEntries(LocalDateTime.now(), limit));
        appendEntries(xml, "/foro/", discussionRepository.findSitemapEntries(limit));

        xml.append("</urlset>\n");
        return xml.toString();
    }

    /**
     * Noticias, ofertas y debates: su contenido está escrito en un solo idioma (castellano), así que solo se
     * publica esa URL. Las versiones /ca y /en existen para navegar, pero apuntan con canonical a esta.
     */
    private void appendEntries(StringBuilder xml, String section, List<SitemapEntry> entries) {
        for (SitemapEntry entry : entries) {
            appendSingleUrl(xml, section + SlugUtil.idSlug(entry.getId(), entry.getTitle()), entry.getLastModified());
        }
    }

    private void appendSingleUrl(StringBuilder xml, String path, LocalDateTime lastModified) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapeXml(localizedUrl(Languages.DEFAULT, path))).append("</loc>\n");
        appendLastModified(xml, lastModified);
        xml.append("  </url>\n");
    }

    /** Una entrada por idioma, cada una con los enlaces alternativos a todas las versiones (hreflang). */
    private void appendTranslatedUrl(StringBuilder xml, String path) {
        for (Locale locale : Languages.SUPPORTED) {
            xml.append("  <url>\n");
            xml.append("    <loc>").append(escapeXml(localizedUrl(locale, path))).append("</loc>\n");
            for (Locale alternate : Languages.SUPPORTED) {
                xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"").append(alternate.getLanguage())
                        .append("\" href=\"").append(escapeXml(localizedUrl(alternate, path))).append("\"/>\n");
            }
            xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"x-default\" href=\"")
                    .append(escapeXml(localizedUrl(Languages.DEFAULT, path))).append("\"/>\n");
            xml.append("  </url>\n");
        }
    }

    private static void appendLastModified(StringBuilder xml, LocalDateTime lastModified) {
        if (lastModified != null) {
            xml.append("    <lastmod>").append(DateTimeFormatter.ISO_LOCAL_DATE.format(lastModified)).append("</lastmod>\n");
        }
    }

    /** URL pública de una ruta interna en un idioma: «/empleo» → «…/en/jobs». La portada castellana es «…/». */
    private String localizedUrl(Locale locale, String path) {
        return publicUrl + Languages.localizePath(locale, path);
    }

    private String buildRobots() {
        StringBuilder robots = new StringBuilder("User-agent: *\nAllow: /\nDisallow: /api/\nDisallow: /admin\n");
        for (Locale locale : Languages.SUPPORTED) {
            for (String path : List.of("/login", "/registro")) {
                robots.append("Disallow: ").append(Languages.localizePath(locale, path)).append('\n');
            }
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
