package com.nexus.backend.repository;

import com.nexus.backend.entity.User;
import com.nexus.backend.exception.UnauthorizedException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleSubject(String googleSubject);

    /** La persona con sesión iniciada; si su cuenta ya no existe, la sesión deja de valer (401). */
    default User getSessionUser(Long id) {
        return findById(id).orElseThrow(() -> new UnauthorizedException("error.user.notFound"));
    }

    boolean existsByEmail(String email);

    /** Emails de las cuentas de administración activas y verificadas, para los avisos de moderación. */
    @Query("select u.email from User u where u.role = com.nexus.backend.entity.User.Role.ADMIN and u.enabled = true "
            + "and u.emailVerifiedAt is not null and u.deletedAt is null")
    List<String> findAdminEmails();
}
