package com.nexus.backend.service.impl;

import com.nexus.backend.entity.JobOffer;
import com.nexus.backend.entity.User;
import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.WorkMode;
import com.nexus.backend.repository.JobOfferRepository;
import com.nexus.backend.repository.SavedJobRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Ofertas guardadas: privadas por usuario, idempotentes y coherentes cuando una oferta deja de publicarse. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SavedJobsIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JobOfferRepository jobOfferRepository;
    @Autowired
    private SavedJobRepository savedJobRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void requiresLogin() throws Exception {
        mvc.perform(get("/api/saved-jobs")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/saved-jobs/ids")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/saved-jobs/1")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/saved-jobs/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void savesListsAndRemovesIdempotently() throws Exception {
        String ana = bearer(user());
        Long first = job("Primera", true).getId();
        Long second = job("Segunda", true).getId();

        mvc.perform(put("/api/saved-jobs/" + first).header("Authorization", ana)).andExpect(status().isNoContent());
        mvc.perform(put("/api/saved-jobs/" + second).header("Authorization", ana)).andExpect(status().isNoContent());
        // Guardar dos veces no duplica.
        mvc.perform(put("/api/saved-jobs/" + first).header("Authorization", ana)).andExpect(status().isNoContent());

        mvc.perform(get("/api/saved-jobs/ids").header("Authorization", ana))
                .andExpect(jsonPath("$", contains(second.intValue(), first.intValue())));
        mvc.perform(get("/api/saved-jobs").header("Authorization", ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].job.title").value("Segunda"))
                .andExpect(jsonPath("$.content[0].available").value(true))
                .andExpect(jsonPath("$.content[0].savedAt").exists());

        mvc.perform(delete("/api/saved-jobs/" + first).header("Authorization", ana)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/saved-jobs/" + first).header("Authorization", ana)).andExpect(status().isNoContent());
        mvc.perform(get("/api/saved-jobs/ids").header("Authorization", ana))
                .andExpect(jsonPath("$", contains(second.intValue())));
    }

    @Test
    void eachUserOnlySeesTheirOwn() throws Exception {
        String ana = bearer(user());
        String luis = bearer(user());
        Long id = job("Privada", true).getId();

        mvc.perform(put("/api/saved-jobs/" + id).header("Authorization", ana)).andExpect(status().isNoContent());
        mvc.perform(get("/api/saved-jobs/ids").header("Authorization", luis)).andExpect(jsonPath("$", empty()));
        // Luis no puede quitar el guardado de Ana: solo actúa sobre los suyos.
        mvc.perform(delete("/api/saved-jobs/" + id).header("Authorization", luis)).andExpect(status().isNoContent());
        mvc.perform(get("/api/saved-jobs/ids").header("Authorization", ana))
                .andExpect(jsonPath("$", contains(id.intValue())));
    }

    @Test
    void unpublishedJobsCannotBeSavedButStayListedAsUnavailable() throws Exception {
        String ana = bearer(user());
        Long hidden = job("Inactiva", false).getId();
        mvc.perform(put("/api/saved-jobs/" + hidden).header("Authorization", ana)).andExpect(status().isNotFound());
        mvc.perform(put("/api/saved-jobs/999999").header("Authorization", ana)).andExpect(status().isNotFound());

        JobOffer job = job("Se desactivará", true);
        mvc.perform(put("/api/saved-jobs/" + job.getId()).header("Authorization", ana)).andExpect(status().isNoContent());
        job.setIsActive(false);
        jobOfferRepository.save(job);

        mvc.perform(get("/api/saved-jobs").header("Authorization", ana))
                .andExpect(jsonPath("$.content[0].job.title").value("Se desactivará"))
                .andExpect(jsonPath("$.content[0].available").value(false));
    }

    @Test
    void deletingAJobRemovesItFromSavedLists() throws Exception {
        User admin = user();
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);
        String ana = bearer(user());
        Long id = job("Se borrará", true).getId();
        mvc.perform(put("/api/saved-jobs/" + id).header("Authorization", ana)).andExpect(status().isNoContent());

        mvc.perform(delete("/api/admin/jobs/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        assertThat(jobOfferRepository.existsById(id)).isFalse();
        mvc.perform(get("/api/saved-jobs/ids").header("Authorization", ana)).andExpect(jsonPath("$", empty()));
        assertThat(savedJobRepository.findAll()).noneMatch(saved -> saved.getJob().getId().equals(id));
    }

    private User user() {
        User user = new User();
        user.setName("Usuario de prueba");
        user.setEmail("saved-" + UUID.randomUUID() + "@test.com");
        user.setPassword(passwordEncoder.encode("Clave12345"));
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private String bearer(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
    }

    private JobOffer job(String title, boolean active) {
        JobOffer job = new JobOffer();
        job.setTitle(title);
        job.setCompany("Empresa");
        job.setDescription("Descripción de la oferta");
        job.setLocation("Barcelona");
        job.setWorkMode(WorkMode.REMOTE);
        job.setContractType(ContractType.FULL_TIME);
        job.setLevel(JobLevel.JUNIOR);
        job.setApplyUrl("https://example.com/oferta");
        job.setIsActive(active);
        job.setPublishedAt(LocalDateTime.now());
        return jobOfferRepository.save(job);
    }
}
