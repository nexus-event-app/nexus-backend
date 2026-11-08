package com.nexus.backend.importer.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.ImportSourceType;
import com.nexus.backend.enums.WorkMode;
import com.nexus.backend.importer.FeedClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * API pública de Jobicy (https://github.com/Jobicy/remote-jobs-api). Sus condiciones piden no consultarla más
 * de una vez por hora, mantener a Jobicy como fuente con su enlace original y no presentar las ofertas como propias.
 * Todas las ofertas son en remoto; el campo {@code jobGeo} indica desde dónde se puede aplicar.
 */
@Component
@RequiredArgsConstructor
public class JobicyJobProvider implements JobFeedProvider {

    private final FeedClient feedClient;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(ImportSourceType type) {
        return type == ImportSourceType.JOBICY_JOBS;
    }

    @Override
    public List<ImportedJob> fetch(ImportSource source) {
        return parse(JsonFeeds.read(objectMapper, feedClient.get(source.getUrl(), "application/json")));
    }

    List<ImportedJob> parse(JsonNode root) {
        return JsonFeeds.stream(root.path("jobs"))
                .filter(job -> JsonFeeds.hasText(job, "id", "jobTitle", "companyName", "url"))
                .map(this::toImportedJob)
                .toList();
    }

    private ImportedJob toImportedJob(JsonNode job) {
        List<String> geo = values(job.path("jobGeo"));
        String location = geo.isEmpty() ? "Remoto" : "Remoto (" + String.join(", ", geo) + ")";
        String description = JsonFeeds.text(job, "jobDescription");
        if (description == null) {
            description = JsonFeeds.text(job, "jobExcerpt");
        }
        List<String> levelHints = values(job.path("jobLevel"));
        return new ImportedJob(
                ExternalIds.of("jobicy", JsonFeeds.text(job, "id")),
                JsonFeeds.text(job, "jobTitle"),
                JsonFeeds.text(job, "companyName"),
                description,
                location,
                WorkMode.REMOTE,
                contractType(values(job.path("jobType"))),
                List.of(),
                JsonFeeds.text(job, "url"),
                publishedAt(JsonFeeds.text(job, "pubDate")),
                levelHints,
                false);
    }

    /** Jobicy devuelve algunos campos como texto y otros como lista: se aceptan ambos. */
    private static List<String> values(JsonNode node) {
        if (node.isArray()) {
            return JsonFeeds.textList(node);
        }
        if (node.isValueNode() && !node.asText().isBlank()) {
            List<String> single = new ArrayList<>();
            for (String part : node.asText().split(",")) {
                if (!part.isBlank()) {
                    single.add(part.trim());
                }
            }
            return single;
        }
        return List.of();
    }

    /** La fecha llega como «2026-11-04 16:53:35» o ISO. */
    private static LocalDateTime publishedAt(String value) {
        return value == null ? null : JsonFeeds.dateTime(value.trim().replace(' ', 'T'));
    }

    private static ContractType contractType(List<String> types) {
        String joined = String.join(" ", types).toLowerCase();
        if (joined.contains("intern")) {
            return ContractType.INTERNSHIP;
        }
        if (joined.contains("part")) {
            return ContractType.PART_TIME;
        }
        if (joined.contains("freelance")) {
            return ContractType.FREELANCE;
        }
        if (joined.contains("contract") || joined.contains("temporary")) {
            return ContractType.TEMPORARY;
        }
        return ContractType.FULL_TIME;
    }
}
