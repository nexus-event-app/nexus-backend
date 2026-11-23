package com.nexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "testimonials", indexes = {
        @Index(name = "idx_testimonials_created_at", columnList = "created_at"),
        @Index(name = "idx_testimonials_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Testimonial extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String role;

    @Column(length = 100)
    private String location;

    @Column(nullable = false, length = 300)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_active")
    private Boolean isActive = false;

    @Column(name = "is_pending")
    private Boolean isPending = true;

    @Column(name = "is_highlighted")
    private Boolean isHighlighted = false;

    @Column(name = "held", nullable = false)
    private boolean held;

    public boolean isPublished() {
        return Boolean.TRUE.equals(isActive) && !Boolean.TRUE.equals(isPending);
    }
}
