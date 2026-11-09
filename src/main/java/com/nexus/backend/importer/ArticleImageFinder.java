package com.nexus.backend.importer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Busca la imagen de portada de un artículo en su propia página (la que usan las redes sociales al compartirlo:
 * {@code og:image}, {@code twitter:image}...). Se usa cuando el feed no trae imagen. Pasa por {@link FeedClient},
 * con las mismas protecciones (solo direcciones públicas, tamaño y redirecciones limitados).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleImageFinder {

    static final int MAX_URL_LENGTH = 500;
    private static final List<String> SELECTORS = List.of(
            "meta[property=og:image:secure_url]",
            "meta[property=og:image]",
            "meta[name=og:image]",
            "meta[name=twitter:image]",
            "meta[property=twitter:image]",
            "meta[name=twitter:image:src]",
            "link[rel=image_src]");

    private final FeedClient feedClient;

    public Optional<String> find(String articleUrl) {
        try {
            byte[] html = feedClient.get(articleUrl, "text/html,application/xhtml+xml");
            if (html == null || html.length == 0) {
                return Optional.empty();
            }
            return extract(new String(html, StandardCharsets.UTF_8), articleUrl);
        } catch (RuntimeException e) {
            log.debug("No se pudo obtener la imagen de {}: {}", articleUrl, e.getMessage());
            return Optional.empty();
        }
    }

    /** Primera imagen declarada en la cabecera de la página, como dirección https absoluta. */
    static Optional<String> extract(String html, String pageUrl) {
        Document document = Jsoup.parse(html, pageUrl);
        for (String selector : SELECTORS) {
            Optional<String> found = document.select(selector).stream()
                    .map(element -> element.hasAttr("content") ? element.attr("content") : element.attr("href"))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .map(value -> absolute(pageUrl, value))
                    .flatMap(Optional::stream)
                    .findFirst();
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private static Optional<String> absolute(String pageUrl, String value) {
        try {
            String url = URI.create(pageUrl).resolve(value.replace(" ", "%20")).toString();
            return url.startsWith("https://") && url.length() <= MAX_URL_LENGTH ? Optional.of(url) : Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
