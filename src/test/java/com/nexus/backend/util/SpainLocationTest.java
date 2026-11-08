package com.nexus.backend.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SpainLocationTest {

    @Test
    void recognizesSpainAndItsMainCities() {
        assertThat(SpainLocation.matches("Madrid, España")).isTrue();
        assertThat(SpainLocation.matches("Remoto (España)")).isTrue();
        assertThat(SpainLocation.matches("Barcelona")).isTrue();
        assertThat(SpainLocation.matches("Málaga")).isTrue();
        assertThat(SpainLocation.matches("Remote, Spain")).isTrue();
    }

    @Test
    void doesNotTreatOtherPlacesAsSpain() {
        assertThat(SpainLocation.matches("Santiago, Chile")).isFalse();
        assertThat(SpainLocation.matches("Remoto")).isFalse();
        assertThat(SpainLocation.matches("Worldwide")).isFalse();
        assertThat(SpainLocation.matches("Valencia, Venezuela")).isFalse();
        assertThat(SpainLocation.matches("Ciudad de México")).isFalse();
        assertThat(SpainLocation.matches("")).isFalse();
        assertThat(SpainLocation.matches(null)).isFalse();
    }
}
