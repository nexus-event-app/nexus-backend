package com.nexus.backend.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpanishCitiesTest {

    @Test
    void findsTheCityInsideAnAddress() {
        assertThat(SpanishCities.find("42 Málaga Fundación Telefónica Av de Sor Teresa Prat, 15, Carretera de Cádiz, 2"))
                .contains("Málaga");
        assertThat(SpanishCities.find("Doofinder C. de Cronos, 63 · Madrid")).contains("Madrid");
        assertThat(SpanishCities.find("Espacio RES Av. de la República Argentina, 25, 41011 Sevilla, Spain")).contains("Sevilla");
        assertThat(SpanishCities.find("Barcelona/Espacio por confirmar")).contains("Barcelona");
        assertThat(SpanishCities.find("Las Palmas de Gran Canaria")).contains("Las Palmas de Gran Canaria");
        assertThat(SpanishCities.find("a coruña")).contains("A Coruña");
        assertThat(SpanishCities.find("Un sitio sin ciudad conocida")).isEmpty();
    }

    @Test
    void tellsAnAddressFromACity() {
        assertThat(SpanishCities.looksLikeAddress("Madrid")).isFalse();
        assertThat(SpanishCities.looksLikeAddress("Santiago de Compostela")).isFalse();
        assertThat(SpanishCities.looksLikeAddress("Calle Mayor, 1")).isTrue();
        assertThat(SpanishCities.looksLikeAddress("Doofinder C. de Cronos 63")).isTrue();
    }
}
