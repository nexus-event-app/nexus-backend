package com.nexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "import_exclusions")
@Getter
@Setter
@NoArgsConstructor
public class ImportExclusion extends CreatedEntity {

    @Id
    @Column(name = "external_id", length = 200)
    private String externalId;

    public ImportExclusion(String externalId) {
        this.externalId = externalId;
    }
}
