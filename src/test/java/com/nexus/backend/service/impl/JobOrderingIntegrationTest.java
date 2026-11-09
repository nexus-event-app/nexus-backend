package com.nexus.backend.service.impl;

import com.nexus.backend.dto.response.JobResponse;
import com.nexus.backend.entity.JobOffer;
import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.WorkMode;
import com.nexus.backend.repository.JobOfferRepository;
import com.nexus.backend.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Orden público de las ofertas: España primero y, dentro de cada grupo, por fecha de publicación real. */
@SpringBootTest
@ActiveProfiles("test")
class JobOrderingIntegrationTest {

    @Autowired
    private JobOfferRepository jobOfferRepository;
    @Autowired
    private JobService jobService;

    @BeforeEach
    void seed() {
        jobOfferRepository.deleteAll();
        LocalDateTime now = LocalDateTime.now();
        // Se guardan en un orden distinto al esperado: la fecha de importación (createdAt) no debe influir.
        save("Chile reciente", "Santiago, Chile", now.minusDays(1));
        save("Valencia antigua", "Valencia", now.minusDays(10));
        save("Chile antigua", "Chile", now.minusDays(8));
        save("Girona reciente", "Girona", now.minusDays(2));
        save("Remoto intermedia", "Remoto", now.minusDays(4));
    }

    @Test
    void publicListShowsSpainFirstThenNewestPublication() {
        assertThat(jobService.getPublicJobs(null, null, null, null, 0, 10).getContent())
                .extracting(JobResponse::getTitle)
                .containsExactly("Girona reciente", "Valencia antigua", "Chile reciente", "Remoto intermedia", "Chile antigua");
        assertThat(jobService.getLatestJobs(3)).extracting(JobResponse::getTitle)
                .containsExactly("Girona reciente", "Valencia antigua", "Chile reciente");
    }

    @Test
    void adminListIsOrderedByPublicationDate() {
        assertThat(jobService.getAllJobs(null, null, null, null, 0, 10).getContent())
                .extracting(JobResponse::getTitle)
                .containsExactly("Chile reciente", "Girona reciente", "Remoto intermedia", "Chile antigua", "Valencia antigua");
    }

    private void save(String title, String location, LocalDateTime publishedAt) {
        JobOffer job = new JobOffer();
        job.setTitle(title);
        job.setCompany("Empresa");
        job.setDescription("Descripción de la oferta");
        job.setLocation(location);
        job.setWorkMode(WorkMode.ONSITE);
        job.setContractType(ContractType.FULL_TIME);
        job.setLevel(JobLevel.JUNIOR);
        job.setApplyUrl("https://example.com/oferta");
        job.setIsActive(true);
        job.setPublishedAt(publishedAt);
        jobOfferRepository.save(job);
    }
}
