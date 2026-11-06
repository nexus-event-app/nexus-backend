package com.nexus.backend.service.impl;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ImportSourceServiceImplTest {

    @Test
    void humanizesRemainingCooldown() {
        assertThat(ImportSourceServiceImpl.humanize(Duration.ofSeconds(20))).isEqualTo("1 min");
        assertThat(ImportSourceServiceImpl.humanize(Duration.ofMinutes(45))).isEqualTo("45 min");
        assertThat(ImportSourceServiceImpl.humanize(Duration.ofMinutes(360))).isEqualTo("6 h");
        assertThat(ImportSourceServiceImpl.humanize(Duration.ofMinutes(125))).isEqualTo("2 h 5 min");
    }
}
