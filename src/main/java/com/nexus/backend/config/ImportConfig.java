package com.nexus.backend.config;

import com.nexus.backend.importer.ImportProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(ImportProperties.class)
public class ImportConfig {

    /** El planificador solo se activa si la importación automática está habilitada. */
    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(prefix = "app.import", name = "enabled", havingValue = "true")
    static class SchedulingConfig {
    }
}
