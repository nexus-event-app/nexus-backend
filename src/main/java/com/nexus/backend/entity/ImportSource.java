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
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_sources")
@Getter
@Setter
@NoArgsConstructor
public class ImportSource extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private ImportSourceType type;

    @Column(nullable = false, length = 500)
    private String url;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "news_category", length = 30)
    private NewsCategory newsCategory;

    @Column(name = "auto_publish", nullable = false)
    private Boolean autoPublish = true;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "remote_only", nullable = false)
    private Boolean remoteOnly = false;

    @Column(name = "language", nullable = false, length = 5)
    private String language = "es";

    @Column(length = 500)
    private String keywords;

    @Column(name = "exclude_keywords", length = 500)
    private String excludeKeywords;

    @Column(name = "use_source_images", nullable = false)
    private Boolean useSourceImages = true;

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
}
