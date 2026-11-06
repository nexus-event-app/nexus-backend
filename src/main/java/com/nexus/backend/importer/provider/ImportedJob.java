package com.nexus.backend.importer.provider;

import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.WorkMode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Oferta normalizada a partir de una fuente externa, antes de filtrarla y guardarla.
 *
 * @param levelHints  textos del origen que indican el nivel (p. ej. "entry", "internship")
 * @param techSource  true si el origen ya garantiza que la oferta es del sector tech
 */
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
