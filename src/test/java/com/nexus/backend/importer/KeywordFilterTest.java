package com.nexus.backend.importer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordFilterTest {

    @Test
    void withoutKeywordsEverythingMatches() {
        assertThat(KeywordFilter.of(null).matches("lo que sea")).isTrue();
        assertThat(KeywordFilter.of(" , ").matches("lo que sea")).isTrue();
    }

    @Test
    void matchesWholeWordsIgnoringCaseAndAccents() {
        KeywordFilter filter = KeywordFilter.of("programación, IA, open source");

        assertThat(filter.matches("Guía de PROGRAMACION para empezar")).isTrue();
        assertThat(filter.matches("La IA llega a los editores de código")).isTrue();
        assertThat(filter.matches("Proyectos open source para tu portfolio")).isTrue();
        assertThat(filter.matches("Un dinosaurio preservado durante millones de años")).isFalse();
        assertThat(filter.matches("Una media de tres horas")).as("'ia' dentro de otra palabra no cuenta").isFalse();
    }

    @Test
    void exclusionWithoutKeywordsExcludesNothing() {
        assertThat(KeywordFilter.exclusion(null).matches("cualquier cosa")).isFalse();
        assertThat(KeywordFilter.exclusion(" ").matches("cualquier cosa")).isFalse();
        assertThat(KeywordFilter.exclusion("apuestas, cripto").matches("Las apuestas online crecen")).isTrue();
        assertThat(KeywordFilter.exclusion("apuestas, cripto").matches("Aprende Python desde cero")).isFalse();
    }
}
