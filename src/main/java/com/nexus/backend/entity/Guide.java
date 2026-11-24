package com.nexus.backend.entity;

import jakarta.persistence.Column;
import com.nexus.backend.enums.GuideCategory;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "guides")
@Getter
@Setter
@NoArgsConstructor
public class Guide extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "title_ca", length = 200)
    private String titleCa;

    @Column(name = "title_en", length = 200)
    private String titleEn;

    @Column(name = "description_ca", length = 300)
    private String descriptionCa;

    @Column(name = "description_en", length = 300)
    private String descriptionEn;

    @Column(name = "content_ca", columnDefinition = "TEXT")
    private String contentCa;

    @Column(name = "content_en", columnDefinition = "TEXT")
    private String contentEn;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private GuideCategory category = GuideCategory.PRIMEROS_PASOS;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
