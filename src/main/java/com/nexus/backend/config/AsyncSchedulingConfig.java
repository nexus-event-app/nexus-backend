package com.nexus.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Tareas programadas (importación, limpieza de tokens) y envío de emails en segundo plano.
 * Cada tarea decide si se activa con su propia propiedad.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncSchedulingConfig {
}
