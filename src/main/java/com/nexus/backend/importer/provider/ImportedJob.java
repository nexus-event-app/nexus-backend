package com.nexus.backend.importer.provider;

import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.WorkMode;

import java.time.LocalDateTime;
import java.util.List;

public record ImportedJob(
        String externalId,
        String title,
        String company,
        String description,
        String location,
        WorkMode workMode,
        ContractType contractType,
        List<String> tags,
        String applyUrl,
        LocalDateTime publishedAt,
        List<String> levelHints,
        boolean techSource) {
}
