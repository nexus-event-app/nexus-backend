package com.nexus.backend.importer.provider;

import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.enums.ImportSourceType;
import com.nexus.backend.importer.FeedClient;
import com.nexus.backend.importer.ImageUrlPolicy;
import com.nexus.backend.importer.ImportException;
import com.nexus.backend.util.HtmlText;
import com.nexus.backend.util.TextUtil;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/** Lee feeds RSS 2.0 y Atom. Solo guarda un resumen: el artículo completo se lee en el origen. */
@Component
@RequiredArgsConstructor
public class RssNewsProvider implements NewsFeedProvider {

    static final int SUMMARY_MAX_LENGTH = 600;
    private static final String MEDIA_NAMESPACE = "http://search.yahoo.com/mrss/";
    private static final String ACCEPT = "application/rss+xml, application/atom+xml, application/xml;q=0.9, text/xml;q=0.8";

    private final FeedClient feedClient;

    @Override
    public boolean supports(ImportSourceType type) {
        return type == ImportSourceType.RSS_NEWS;
    }

    @Override
    public List<ImportedNews> fetch(ImportSource source) {
        return parse(feedClient.get(source.getUrl(), ACCEPT));
    }

    List<ImportedNews> parse(byte[] xml) {
        SyndFeed feed;
        try {
            SyndFeedInput input = new SyndFeedInput();
            input.setAllowDoctypes(false);
            feed = input.build(new XmlReader(new ByteArrayInputStream(xml)));
        } catch (FeedException | IOException | IllegalArgumentException e) {
            throw new ImportException("La fuente no es un feed RSS o Atom válido", e);
        }
        return feed.getEntries().stream()
                .map(this::toImportedNews)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ImportedNews> toImportedNews(SyndEntry entry) {
        String title = TextUtil.optionalLine(HtmlText.toPlainText(entry.getTitle()));
        String link = TextUtil.optionalLine(entry.getLink());
        if (title == null || link == null || !link.startsWith("http")) {
            return Optional.empty();
        }
        String html = bodyHtml(entry);
        String summary = TextUtil.truncate(HtmlText.toPlainText(html), SUMMARY_MAX_LENGTH);
        String identity = Optional.ofNullable(TextUtil.optionalLine(entry.getUri())).orElse(link);

        return Optional.of(new ImportedNews(
                ExternalIds.hashed("rss", identity),
                title,
                summary,
                link,
                imageOf(entry, html),
                TextUtil.optionalLine(entry.getAuthor()),
                toLocalDateTime(entry.getPublishedDate() != null ? entry.getPublishedDate() : entry.getUpdatedDate())));
    }

    /** Prefiere el resumen; si no lo hay, el contenido completo (que luego se recorta). */
    private static String bodyHtml(SyndEntry entry) {
        SyndContent description = entry.getDescription();
        if (description != null && description.getValue() != null && !description.getValue().isBlank()) {
            return description.getValue();
        }
        return entry.getContents().stream()
                .map(SyndContent::getValue)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    private static String imageOf(SyndEntry entry, String html) {
        Stream<String> media = entry.getForeignMarkup().stream()
                .filter(element -> MEDIA_NAMESPACE.equals(element.getNamespaceURI()))
                .filter(element -> element.getName().equals("content") || element.getName().equals("thumbnail"))
                .filter(element -> element.getAttributeValue("medium") == null
                        || "image".equals(element.getAttributeValue("medium")))
                .map(element -> element.getAttributeValue("url"));
        Stream<String> enclosures = entry.getEnclosures().stream()
                .filter(enclosure -> enclosure.getType() != null && enclosure.getType().startsWith("image/"))
                .map(SyndEnclosure::getUrl);
        return Stream.concat(media, enclosures)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(ImageUrlPolicy::isAcceptable)
                .findFirst()
                .or(() -> HtmlText.firstImage(html).filter(ImageUrlPolicy::isAcceptable))
                .orElse(null);
    }

    private static LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

}
