package com.nexus.backend.entity;

import com.nexus.backend.enums.NewsCategory;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "news", indexes = {
        @Index(name = "idx_news_created_at", columnList = "created_at"),
        @Index(name = "idx_news_category", columnList = "category"),
        @Index(name = "idx_news_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 100)
    private String author;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "category", nullable = false, length = 30)
    private NewsCategory category = NewsCategory.GENERAL;

    @Column(name = "is_active")
    private Boolean isActive = true;

    /** Origen de la noticia cuando se importa automáticamente (null para noticias manuales). */
    /** Idioma del texto: «es» o «en» (ContentLanguage). */
    @Column(name = "language", nullable = false, length = 5)
    private String language = "es";

    @Column(name = "source", length = 100)
    private String source;

    /** Identificador en el origen externo; único para evitar importaciones duplicadas. */
    @Column(name = "external_id", unique = true, length = 200)
    private String externalId;

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
