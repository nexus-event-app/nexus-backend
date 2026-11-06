package com.nexus.backend.importer;

import com.nexus.backend.enums.JobLevel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JobClassifierTest {

    @Test
    void detectsLevelFromTitleInSeveralLanguages() {
        assertThat(JobClassifier.level("Junior Frontend Developer", List.of())).contains(JobLevel.JUNIOR);
        assertThat(JobClassifier.level("Desarrollador Jr. Java", List.of())).contains(JobLevel.JUNIOR);
        assertThat(JobClassifier.level("Prácticas de desarrollo web", List.of())).contains(JobLevel.INTERN);
        assertThat(JobClassifier.level("Becario/a de QA", List.of())).contains(JobLevel.INTERN);
        assertThat(JobClassifier.level("Werkstudent Softwareentwicklung", List.of())).contains(JobLevel.INTERN);
        assertThat(JobClassifier.level("Software Engineering Intern", List.of())).contains(JobLevel.INTERN);
        assertThat(JobClassifier.level("Práctica Profesional – Ingeniería", List.of())).contains(JobLevel.INTERN);
    }

    @Test
    void seniorTitlesAreRejectedEvenWithJuniorHints() {
        assertThat(JobClassifier.level("Senior Backend Engineer", List.of("entry"))).isEmpty();
        assertThat(JobClassifier.level("Tech Lead Java", List.of())).isEmpty();
        assertThat(JobClassifier.level("Sr. Data Engineer", List.of())).isEmpty();
    }

    @Test
    void usesSourceHintsWhenTitleIsNeutral() {
        assertThat(JobClassifier.level("Frontend Developer", List.of("Full Time", "entry"))).contains(JobLevel.JUNIOR);
        assertThat(JobClassifier.level("Frontend Developer", List.of("Praktikum", "internship"))).contains(JobLevel.INTERN);
        assertThat(JobClassifier.level("Frontend Developer", List.of("professional / experienced"))).isEmpty();
        assertThat(JobClassifier.level("Frontend Developer", List.of())).isEqualTo(Optional.empty());
    }

    @Test
    void dataEntryIsNotAJuniorSignal() {
        assertThat(JobClassifier.level("Data Entry Clerk", List.of())).isEmpty();
    }

    @Test
    void detectsTechJobsFromTitleOrTags() {
        assertThat(JobClassifier.isTech("Junior Fachinformatiker/-in für Anwendungsentwicklung", List.of())).isTrue();
        assertThat(JobClassifier.isTech("Junior Analyst", List.of("Python", "SQL"))).isTrue();
        assertThat(JobClassifier.isTech("Desarrolladora web junior", List.of())).isTrue();
        assertThat(JobClassifier.isTech("Praktikum als HR-Manager", List.of("Human Resources"))).isFalse();
        assertThat(JobClassifier.isTech("Werkstudent:in Influencer & Creator Marketing", List.of())).isFalse();
        assertThat(JobClassifier.isTech("Business Developer junior (H/F) – Agence digitale", List.of())).isFalse();
    }
}
