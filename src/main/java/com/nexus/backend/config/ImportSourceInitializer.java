package com.nexus.backend.config;

import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.importer.DefaultImportSources;
import com.nexus.backend.importer.ImportProperties;
import com.nexus.backend.repository.ImportSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Crea las fuentes recomendadas la primera vez que arranca la aplicación (si no hay ninguna).
 * Después se gestionan desde el panel de administración.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImportSourceInitializer implements CommandLineRunner {

    private final ImportSourceRepository sourceRepository;
    private final ImportProperties properties;

    @Override
    public void run(String... args) {
        if (!properties.seedDefaults() || sourceRepository.count() > 0) {
            return;
        }
        List<ImportSource> defaults = DefaultImportSources.create();
        sourceRepository.saveAll(defaults);
        log.info("Creadas {} fuentes de importación por defecto", defaults.size());
    }
}
