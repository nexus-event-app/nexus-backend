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

import java.util.List;

/**
 * API pública de Remotive (https://remotive.com/api-documentation). Sus condiciones obligan a enlazar
 * la oferta original y citar a Remotive como fuente, y a no consultarla más de 4 veces al día.
 */
@Component
@RequiredArgsConstructor
public class RemotiveJobProvider implements JobFeedProvider {

    private final FeedClient feedClient;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(ImportSourceType type) {
        return type == ImportSourceType.REMOTIVE_JOBS;
    }

    @Override
    public List<ImportedJob> fetch(ImportSource source) {
        return parse(JsonFeeds.read(objectMapper, feedClient.get(source.getUrl(), "application/json")));
    }

    List<ImportedJob> parse(JsonNode root) {
        return JsonFeeds.stream(root.path("jobs"))
                .filter(job -> JsonFeeds.hasText(job, "id", "title", "company_name", "url"))
                .map(this::toImportedJob)
                .toList();
    }

    private ImportedJob toImportedJob(JsonNode job) {
        String location = JsonFeeds.text(job, "candidate_required_location");
        return new ImportedJob(
                ExternalIds.of("remotive", JsonFeeds.text(job, "id")),
                JsonFeeds.text(job, "title"),
                JsonFeeds.text(job, "company_name"),
                JsonFeeds.text(job, "description"),
                location == null ? "Remoto" : "Remoto (" + location + ")",
                WorkMode.REMOTE,
                contractType(JsonFeeds.text(job, "job_type")),
                JsonFeeds.textList(job.path("tags")),
                JsonFeeds.text(job, "url"),
                JsonFeeds.dateTime(JsonFeeds.text(job, "publication_date")),
                List.of(),
                true);
    }

    private static ContractType contractType(String jobType) {
        if (jobType == null) {
            return ContractType.FULL_TIME;
        }
        return switch (jobType) {
            case "part_time" -> ContractType.PART_TIME;
            case "contract" -> ContractType.TEMPORARY;
            case "freelance" -> ContractType.FREELANCE;
            case "internship" -> ContractType.INTERNSHIP;
            default -> ContractType.FULL_TIME;
        };
    }
}
