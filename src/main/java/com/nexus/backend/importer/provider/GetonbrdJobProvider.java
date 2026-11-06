package com.nexus.backend.importer.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.ImportSourceType;
import com.nexus.backend.enums.WorkMode;
import com.nexus.backend.importer.FeedClient;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Entities;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * API pública de Get on Board (https://www.getonbrd.com): ofertas tech en español con nivel y modalidad
 * explícitos. La URL es la de una categoría, p. ej. {@code /api/v0/categories/programming/jobs}.
 * Se enlaza siempre la oferta original.
 */
@Component
@RequiredArgsConstructor
public class GetonbrdJobProvider implements JobFeedProvider {

    private static final int MAX_PAGES = 3;
    private static final String EXPAND = "expand=%5B%22company%22%2C%22tags%22%5D";

    private final FeedClient feedClient;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(ImportSourceType type) {
        return type == ImportSourceType.GETONBRD_JOBS;
    }

    @Override
    public List<ImportedJob> fetch(ImportSource source) {
        List<ImportedJob> jobs = new ArrayList<>();
        int totalPages = 1;
        for (int page = 1; page <= Math.min(totalPages, MAX_PAGES); page++) {
            JsonNode root = JsonFeeds.read(objectMapper, feedClient.get(pageUrl(source.getUrl(), page), "application/json"));
            jobs.addAll(parse(root));
            totalPages = root.path("meta").path("total_pages").asInt(1);
        }
        return jobs;
    }

    static String pageUrl(String baseUrl, int page) {
        StringBuilder url = new StringBuilder(baseUrl);
        char separator = baseUrl.contains("?") ? '&' : '?';
        if (!baseUrl.contains("expand=")) {
            url.append(separator).append(EXPAND);
            separator = '&';
        }
        return url.append(separator).append("page=").append(page).toString();
    }

    List<ImportedJob> parse(JsonNode root) {
        return JsonFeeds.stream(root.path("data"))
                .filter(job -> JsonFeeds.hasText(job, "id") && JsonFeeds.hasText(job.path("attributes"), "title"))
                .filter(job -> JsonFeeds.text(job.path("links"), "public_url") != null)
                .filter(job -> companyName(job.path("attributes")) != null)
                .map(this::toImportedJob)
                .toList();
    }

    private ImportedJob toImportedJob(JsonNode job) {
        JsonNode attributes = job.path("attributes");
        String remoteModality = JsonFeeds.text(attributes, "remote_modality");
        WorkMode workMode = workMode(remoteModality);
        int modality = attributes.path("modality").path("data").path("id").asInt(1);

        return new ImportedJob(
                ExternalIds.of("getonbrd", JsonFeeds.text(job, "id")),
                JsonFeeds.text(attributes, "title"),
                companyName(attributes),
                descriptionHtml(attributes),
                location(attributes, remoteModality),
                workMode,
                contractType(modality),
                names(attributes.path("tags").path("data")),
                JsonFeeds.text(job.path("links"), "public_url"),
                toDateTime(attributes.path("published_at").asLong(0)),
                levelHints(attributes.path("seniority").path("data").path("id").asInt(0), modality),
                true);
    }

    private static String companyName(JsonNode attributes) {
        return JsonFeeds.text(attributes.path("company").path("data").path("attributes"), "name");
    }

    /** Funciones y requisitos, cada bloque con su encabezado. */
    private static String descriptionHtml(JsonNode attributes) {
        StringBuilder html = new StringBuilder();
        for (String section : List.of("functions", "description", "desirable", "benefits")) {
            String body = attributes.path(section).asText("");
            if (body.isBlank()) {
                continue;
            }
            String headline = attributes.path(section + "_headline").asText("");
            if (!headline.isBlank()) {
                html.append("<h3>").append(Entities.escape(headline)).append("</h3>");
            }
            html.append(body);
        }
        return html.toString();
    }

    private static String location(JsonNode attributes, String remoteModality) {
        List<String> countries = JsonFeeds.textList(attributes.path("countries")).stream()
                .filter(country -> !country.equalsIgnoreCase("Remote"))
                .toList();
        String where = String.join(", ", countries);
        if ("fully_remote".equals(remoteModality) || where.isEmpty()) {
            return where.isEmpty() ? "Remoto" : "Remoto (" + where + ")";
        }
        return "remote_local".equals(remoteModality) ? "Remoto (" + where + ")" : where;
    }

    private static WorkMode workMode(String remoteModality) {
        if (remoteModality == null) {
            return WorkMode.ONSITE;
        }
        return switch (remoteModality) {
            case "fully_remote", "remote_local" -> WorkMode.REMOTE;
            case "hybrid" -> WorkMode.HYBRID;
            default -> WorkMode.ONSITE;
        };
    }

    private static ContractType contractType(int modality) {
        return switch (modality) {
            case 2 -> ContractType.PART_TIME;
            case 3 -> ContractType.FREELANCE;
            case 4 -> ContractType.INTERNSHIP;
            default -> ContractType.FULL_TIME;
        };
    }

    /** Traduce el nivel de Get on Board a las pistas que entiende el clasificador. */
    private static List<String> levelHints(int seniority, int modality) {
        String level = switch (seniority) {
            case 1 -> "no experience required";
            case 2 -> "junior";
            case 3 -> "mid";
            case 4, 5 -> "senior";
            default -> "";
        };
        return Stream.of(level, modality == 4 ? "internship" : "").filter(hint -> !hint.isEmpty()).toList();
    }

    private static List<String> names(JsonNode array) {
        if (!array.isArray()) {
            return List.of();
        }
        return StreamSupport.stream(array.spliterator(), false)
                .map(tag -> JsonFeeds.text(tag.path("attributes"), "name"))
                .filter(name -> name != null)
                .toList();
    }

    private static LocalDateTime toDateTime(long epochSeconds) {
        return epochSeconds <= 0 ? null : LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
    }
}
