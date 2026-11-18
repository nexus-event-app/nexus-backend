package com.nexus.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Reloj de la aplicación, en hora de España: «hoy» es el de quien usa la web (un evento de esta noche no debe
 * desaparecer a medianoche UTC). Las pruebas pueden sustituirlo por uno fijo.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Madrid"));
    }
}
