package com.nexus.backend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SpainLocationTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "Madrid, España", "Remoto (España)", "Remote, Spain", "Barcelona", "Málaga", "Valencia", "Alicante",
            "Murcia", "Valladolid", "Donostia", "San Sebastián", "Girona", "Lleida", "A Coruña", "Vitoria-Gasteiz",
            "Palma de Mallorca", "Santiago de Compostela", "Las Rozas de Madrid", "Sant Cugat del Vallès",
            "L’Hospitalet de Llobregat", "Catalunya", "País Vasco", "Comunitat Valenciana", "Islas Canarias",
            "Remoto (Spain, Portugal)", "Córdoba, España", "Híbrido · Bilbao"})
    void recognizesSpanishLocations(String location) {
        assertThat(SpainLocation.matches(location)).as(location).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Santiago, Chile", "Chile", "Remoto", "Remoto (Europe)", "Worldwide", "Valencia, Venezuela",
            "Córdoba, Argentina", "León, México", "Mérida, Mexico", "Granada, Nicaragua", "Ciudad de México",
            "Lisboa, Portugal", "Bogotá, Colombia", "Toledo, Ohio, United States", "Madridejos-ville"})
    void doesNotTreatOtherPlacesAsSpain(String location) {
        assertThat(SpainLocation.matches(location)).as(location).isFalse();
    }

    @Test
    void handlesEmptyValues() {
        assertThat(SpainLocation.matches("")).isFalse();
        assertThat(SpainLocation.matches("   ")).isFalse();
        assertThat(SpainLocation.matches(null)).isFalse();
    }
}
