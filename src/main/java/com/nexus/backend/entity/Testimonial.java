package com.nexus.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "testimonials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Testimonial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String role;
    
    private String location;
    
    @Column(nullable = false, length = 300)
    private String content;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_pending")
    private Boolean isPending = false;
    
    @Column(name = "is_highlighted")
    private Boolean isHighlighted = false;
    
    @Column(name = "user_ip")
    private String userIp;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}