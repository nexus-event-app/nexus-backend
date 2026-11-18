package com.nexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/** «Me apunto» de una persona a un evento. Solo cuenta para el número que se ve; quién es no se muestra. */
@Entity
@Table(name = "event_interests")
@IdClass(EventInterest.Key.class)
@Getter
@Setter
@NoArgsConstructor
public class EventInterest {

    @Id
    @Column(name = "event_id")
    private Long eventId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public EventInterest(Long eventId, Long userId) {
        this.eventId = eventId;
        this.userId = userId;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Key implements Serializable {
        private Long eventId;
        private Long userId;
    }
}
