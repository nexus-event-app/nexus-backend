package com.nexus.backend.entity;

import com.nexus.backend.enums.EventMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Evento que comparte alguien de la comunidad: un enlace para apuntarse, la fecha y una descripción corta. */
@Entity
@Table(name = "community_events")
@Getter
@Setter
@NoArgsConstructor
public class CommunityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "starts_on", nullable = false)
    private LocalDate startsOn;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EventMode mode;

    @Column(length = 80)
    private String city;

    /** Dirección exacta, opcional (solo en los presenciales). */
    @Column(length = 200)
    private String address;

    /** Organizado por y para mujeres: sale en su propio bloque. */
    @Column(name = "for_women", nullable = false)
    private boolean forWomen;

    /** Se oculta si la moderación lo decide tras un reporte. */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /** Quién lo compartió: para que pueda borrarlo y para moderar; no se muestra en la web. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

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

    /** Se ve en la web: activo y todavía no ha pasado. */
    public boolean isVisibleOn(LocalDate today) {
        return active && !startsOn.isBefore(today);
    }
}
