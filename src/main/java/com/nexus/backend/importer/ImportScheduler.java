package com.nexus.backend.importer;

import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.repository.ImportSourceRepository;
import com.nexus.backend.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * Revisa periódicamente qué fuentes activas toca ejecutar según su intervalo y limpia las ofertas
 * importadas que caducaron hace tiempo. Pensado para una sola instancia: con varias réplicas,
 * activarlo solo en una ({@code IMPORT_ENABLED=false} en el resto).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.import", name = "enabled", havingValue = "true")
public class ImportScheduler {

    private final ImportSourceRepository sourceRepository;
    private final JobOfferRepository jobOfferRepository;
    private final ImportRunner importRunner;
    private final ImportProperties properties;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(initialDelayString = "${app.import.initial-delay}", fixedDelayString = "${app.import.check-interval}")
    public void runDueSources() {
        LocalDateTime now = LocalDateTime.now();
        for (ImportSource source : sourceRepository.findByActiveTrue()) {
            if (isDue(source, now)) {
                ImportRunResult result = importRunner.run(source);
                log.info("Importación de '{}': {}", source.getName(), result.message());
            }
        }
        purgeExpiredJobs(now);
    }

    static boolean isDue(ImportSource source, LocalDateTime now) {
        if (source.getLastRunAt() == null) {
            return true;
        }
        int interval = Math.max(source.getIntervalMinutes(), source.getType().getMinIntervalMinutes());
        return !source.getLastRunAt().plusMinutes(interval).isAfter(now);
    }

    private void purgeExpiredJobs(LocalDateTime now) {
        LocalDateTime before = now.minusDays(properties.jobPurgeAfterDays());
        Integer removed = transactionTemplate.execute(status -> jobOfferRepository.deleteImportedExpiredBefore(before));
        if (removed != null && removed > 0) {
            log.info("Borradas {} ofertas importadas caducadas", removed);
        }
    }
}
