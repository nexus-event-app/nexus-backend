package com.nexus.backend.entity;

import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.WorkMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.nexus.backend.util.SpainLocation;
import com.nexus.backend.util.SpanishCities;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_offers", indexes = {
        @Index(name = "idx_jobs_created_at", columnList = "created_at"),
        @Index(name = "idx_jobs_is_active", columnList = "is_active"),
        @Index(name = "idx_jobs_level", columnList = "level"),
        @Index(name = "idx_jobs_work_mode", columnList = "work_mode"),
        @Index(name = "idx_jobs_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
public class JobOffer extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 120)
    private String company;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 120)
    private String location;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "work_mode", nullable = false, length = 20)
    private WorkMode workMode;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "contract_type", nullable = false, length = 20)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "level", nullable = false, length = 20)
    private JobLevel level;

    @Column(length = 400)
    private String tags;

    @Column(name = "apply_url", nullable = false, length = 500)
    private String applyUrl;

    @Column(name = "source", length = 100)
    private String source;

    @Column(name = "external_id", unique = true, length = 200)
    private String externalId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "in_spain", nullable = false)
    private boolean inSpain;

    @Column(length = 80)
    private String city;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Override
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        inSpain = SpainLocation.matches(location);
        city = SpanishCities.canonical(location).orElse(null);
        if (publishedAt == null) {
            publishedAt = getCreatedAt();
        }
    }

    @Override
    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
        inSpain = SpainLocation.matches(location);
        city = SpanishCities.canonical(location).orElse(null);
    }

    public boolean isPublishedAt(LocalDateTime now) {
        return Boolean.TRUE.equals(isActive) && (expiresAt == null || expiresAt.isAfter(now));
    }
}
