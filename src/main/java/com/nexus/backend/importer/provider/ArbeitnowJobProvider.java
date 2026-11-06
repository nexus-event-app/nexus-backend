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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

/** API pública de Arbeitnow (https://www.arbeitnow.com/blog/job-board-api). Pide enlazar al origen. */
@Component
@RequiredArgsConstructor
public class ArbeitnowJobProvider implements JobFeedProvider {

    private final FeedClient feedClient;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(ImportSourceType type) {
        return type == ImportSourceType.ARBEITNOW_JOBS;
    }

    @Override
    public List<ImportedJob> fetch(ImportSource source) {
        return parse(JsonFeeds.read(objectMapper, feedClient.get(source.getUrl(), "application/json")));
    }

    List<ImportedJob> parse(JsonNode root) {
        return JsonFeeds.stream(root.path("data"))
                .filter(job -> JsonFeeds.hasText(job, "slug", "title", "company_name", "url"))
                .map(this::toImportedJob)
                .toList();
    }

    private ImportedJob toImportedJob(JsonNode job) {
        boolean remote = job.path("remote").asBoolean(false);
        List<String> jobTypes = JsonFeeds.textList(job.path("job_types"));
        String location = JsonFeeds.text(job, "location");
        return new ImportedJob(
                ExternalIds.of("arbeitnow", JsonFeeds.text(job, "slug")),
                JsonFeeds.text(job, "title"),
                JsonFeeds.text(job, "company_name"),
                JsonFeeds.text(job, "description"),
                remote ? (location == null ? "Remoto" : "Remoto (" + location + ")") : location,
                remote ? WorkMode.REMOTE : WorkMode.ONSITE,
                contractType(jobTypes),
                JsonFeeds.textList(job.path("tags")),
                JsonFeeds.text(job, "url"),
                toDateTime(job.path("created_at").asLong(0)),
                jobTypes,
                false);
    }

    private static ContractType contractType(List<String> jobTypes) {
        String joined = String.join(" ", jobTypes).toLowerCase(Locale.ROOT);
        if (joined.contains("intern") || joined.contains("praktikum") || joined.contains("stage")
                || joined.contains("apprentice") || joined.contains("ausbildung")) {
            return ContractType.INTERNSHIP;
        }
        if (joined.contains("werkstudent") || joined.contains("working student") || joined.contains("teilzeit")
                || joined.contains("part")) {
            return ContractType.PART_TIME;
        }
        if (joined.contains("freelance") || joined.contains("freiberuf") || joined.contains("selbstständig")) {
            return ContractType.FREELANCE;
        }
        if (joined.contains("befristet") && !joined.contains("unbefristet") || joined.contains("temporary")
                || joined.contains("fixed-term") || joined.contains("fixed term")) {
            return ContractType.TEMPORARY;
        }
        return ContractType.FULL_TIME;
    }

    private static LocalDateTime toDateTime(long epochSeconds) {
        return epochSeconds <= 0 ? null : LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
    }
}
