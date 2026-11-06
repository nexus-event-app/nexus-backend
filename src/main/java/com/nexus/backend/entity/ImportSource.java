package com.nexus.backend.entity;

import com.nexus.backend.enums.ImportSourceType;
import com.nexus.backend.enums.ImportStatus;
import com.nexus.backend.enums.NewsCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Fuente externa desde la que se importan noticias u ofertas de empleo de forma periódica. */
@Entity
@Table(name = "import_sources")
@Getter
@Setter
@NoArgsConstructor
public class ImportSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre visible; se muestra como atribución en el contenido importado. */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private ImportSourceType type;

    @Column(nullable = false, length = 500)
    private String url;

    /** Categoría asignada a las noticias importadas (solo fuentes de noticias). */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "news_category", length = 30)
    private NewsCategory newsCategory;

    /** Si es false, lo importado queda oculto hasta que un admin lo publique. */
    @Column(name = "auto_publish", nullable = false)
    private Boolean autoPublish = true;

    @Column(nullable = false)
    private Boolean active = true;

    /** Solo ofertas en remoto (fuentes de empleo). */
    @Column(name = "remote_only", nullable = false)
    private Boolean remoteOnly = false;

    /**
     * Palabras clave separadas por comas. Si hay alguna, solo se importa lo que contenga al menos una
     * en el título o el resumen (útil para feeds generalistas).
     */
    @Column(length = 500)
    private String keywords;

    @Column(name = "interval_minutes", nullable = false)
    private Integer intervalMinutes;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "last_status", length = 20)
    private ImportStatus lastStatus;

    @Column(name = "last_message", length = 500)
    private String lastMessage;

    @Column(name = "last_imported_count")
    private Integer lastImportedCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
