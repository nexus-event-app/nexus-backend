package com.nexus.backend.service.impl;

import com.nexus.backend.dto.response.ImportSourceResponse;
import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.importer.DefaultImportSources;
import com.nexus.backend.repository.ImportSourceRepository;
import com.nexus.backend.service.ImportSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** «Añadir fuentes recomendadas» crea solo las que faltan y no toca las existentes. */
@SpringBootTest
@ActiveProfiles("test")
class DefaultImportSourcesIntegrationTest {

    @Autowired
    private ImportSourceService importSourceService;
    @Autowired
    private ImportSourceRepository sourceRepository;

    @BeforeEach
    void clean() {
        sourceRepository.deleteAll();
    }

    @Test
    void addsOnlyMissingDefaultsAndKeepsCustomizedOnes() {
        ImportSource customized = DefaultImportSources.create().stream()
                .filter(source -> source.getName().equals("Genbeta")).findFirst().orElseThrow();
        customized.setKeywords("mi filtro");
        customized.setActive(false);
        sourceRepository.save(customized);

        assertThat(importSourceService.addMissingDefaults()).extracting(ImportSourceResponse::getName)
                .hasSize(DefaultImportSources.create().size() - 1)
                .doesNotContain("Genbeta")
                .contains("Jobicy · Europa", "freeCodeCamp en español");

        ImportSource genbeta = sourceRepository.findAll().stream()
                .filter(source -> source.getName().equals("Genbeta")).findFirst().orElseThrow();
        assertThat(genbeta.getKeywords()).isEqualTo("mi filtro");
        assertThat(genbeta.getActive()).isFalse();

        assertThat(importSourceService.addMissingDefaults()).as("segunda vez, nada que añadir").isEmpty();
    }
}
