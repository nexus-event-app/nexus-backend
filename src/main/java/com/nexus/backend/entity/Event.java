package com.nexus.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime eventDate;
    
    @Column(nullable = false)
    private String location; // Barcelona, Madrid, Online, España
    
    @Column(nullable = false)
    private String eventType; // NETWORKING, WORKSHOP, TALK, COFFEE_CHAT
    
    @Column(nullable = false)
    private String level; // JUNIOR_FRIENDLY, ALL_LEVELS
    
    private String tags; // #IA, #React, #Career (separados por comas)
    
    private String sourceUrl; // URL del evento original (Meetup, Eventbrite, etc.)
    
    private String organizerName; // Nombre del organizador
    
    private Boolean isActive; // Si el evento está activo o archivado
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}