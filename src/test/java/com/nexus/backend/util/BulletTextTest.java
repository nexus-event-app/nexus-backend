package com.nexus.backend.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BulletTextTest {

    @Test
    void joinsEachLonelyBulletWithItsTextAndKeepsTheListTogether() {
        String stored = "What you will do\n\n•\n\nPlan usability tests.\n\n•\n\nRun interviews.\n\nAbout us";
        assertThat(BulletText.fix(stored))
                .isEqualTo("What you will do\n\n• Plan usability tests.\n• Run interviews.\n\nAbout us");
    }

    @Test
    void leavesTextsWithoutBulletsOrAlreadyWellFormedAsTheyAre() {
        assertThat(BulletText.fix("Sin listas.\n\nOtro párrafo.")).isEqualTo("Sin listas.\n\nOtro párrafo.");
        assertThat(BulletText.fix("• React\n• CSS")).isEqualTo("• React\n• CSS");
        assertThat(BulletText.fix(null)).isNull();
    }
}
