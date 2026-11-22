package com.nexus.backend.entity;

import com.nexus.backend.enums.DiscussionCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "discussions", indexes = {
        @Index(name = "idx_discussions_created_at", columnList = "created_at"),
        @Index(name = "idx_discussions_category", columnList = "category"),
        @Index(name = "idx_discussions_is_active", columnList = "is_active"),
        @Index(name = "idx_discussions_author", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Discussion extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "category", nullable = false, length = 30)
    private DiscussionCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    @Column(name = "accepted_comment_id")
    private Long acceptedCommentId;

    public boolean isVisible() {
        return Boolean.TRUE.equals(isActive);
    }
}
