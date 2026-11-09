package com.nexus.backend.entity;

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

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean enabled = true;

    /** Cuándo confirmó su email; null mientras no lo haya verificado. */
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    /** Último cambio de contraseña: los JWT emitidos antes dejan de ser válidos. */
    @Column(name = "credentials_changed_at")
    private LocalDateTime credentialsChangedAt;

    /** Cuándo aceptó el aviso legal y la política de privacidad al registrarse. */
    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

    /** Perfil público (todo opcional). */
    @Column(length = 80)
    private String headline;

    @Column(length = 300)
    private String bio;

    /** Tecnologías separadas por comas, en el orden en que la persona las escribió. */
    @Column(length = 400)
    private String technologies;

    @Column(name = "github_url", length = 200)
    private String githubUrl;

    @Column(name = "linkedin_url", length = 200)
    private String linkedinUrl;

    /** Ruta pública de la foto ya procesada («/uploads/avatars/...»). */
    @Column(name = "avatar_url", length = 200)
    private String avatarUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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

    public enum Role {
        USER,
        ADMIN
    }

    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }
}
