package com.nexus.backend.importer;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.import")
public record ImportProperties(
        boolean seedDefaults,
        int newsMaxAgeDays,
        int newsMaxItemsPerRun,
        int jobMaxAgeDays,
        int jobTtlDays,
        int jobMaxItemsPerRun,
        int jobPurgeAfterDays) {
}
