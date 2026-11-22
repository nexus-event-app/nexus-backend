package com.nexus.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "saved_jobs",
        uniqueConstraints = @UniqueConstraint(name = "uk_saved_jobs_user_job", columnNames = {"user_id", "job_id"}),
        indexes = @Index(name = "idx_saved_jobs_job", columnList = "job_id"))
@Getter
@Setter
@NoArgsConstructor
public class SavedJob extends CreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private JobOffer job;

    public SavedJob(User user, JobOffer job) {
        this.user = user;
        this.job = job;
    }
}
