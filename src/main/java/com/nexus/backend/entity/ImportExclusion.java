package com.nexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Identificador externo de un elemento importado que un admin ha borrado. Evita que la siguiente
 * importación lo vuelva a crear.
 */
@Entity
@Table(name = "import_exclusions")
@Getter
@Setter
@NoArgsConstructor
public class ImportExclusion {

    @Id
    @Column(name = "external_id", length = 200)
    private String externalId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ImportExclusion(String externalId) {
        this.externalId = externalId;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
