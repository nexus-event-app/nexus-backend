package com.nexus.backend.importer;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuración de la importación automática ({@code app.import.*}).
 *
 * @param enabled             activa el planificador
 * @param checkInterval       cada cuánto se revisa qué fuentes toca ejecutar
 * @param initialDelay        espera tras arrancar antes de la primera revisión
 * @param seedDefaults        crea las fuentes por defecto si no hay ninguna
 * @param newsMaxAgeDays      no se importan noticias más antiguas
 * @param newsMaxItemsPerRun  máximo de noticias nuevas por fuente y ejecución
 * @param jobMaxAgeDays       no se importan ofertas más antiguas
 * @param jobTtlDays          días que una oferta importada sigue visible desde su publicación
 * @param jobMaxItemsPerRun   máximo de ofertas nuevas por fuente y ejecución
 * @param jobPurgeAfterDays   días tras caducar en que se borran las ofertas importadas
 */
@ConfigurationProperties(prefix = "app.import")
public record ImportProperties(
        boolean enabled,
        Duration checkInterval,
        Duration initialDelay,
        boolean seedDefaults,
        int newsMaxAgeDays,
        int newsMaxItemsPerRun,
        int jobMaxAgeDays,
        int jobTtlDays,
        int jobMaxItemsPerRun,
        int jobPurgeAfterDays) {
}
