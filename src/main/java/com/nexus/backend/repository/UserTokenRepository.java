package com.nexus.backend.repository;

import com.nexus.backend.entity.UserToken;
import com.nexus.backend.enums.UserTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    @Query("select t from UserToken t join fetch t.user where t.tokenHash = :hash and t.type = :type")
    Optional<UserToken> findByHashAndType(@Param("hash") String tokenHash, @Param("type") UserTokenType type);

    /** Invalida los tokens pendientes de un usuario y tipo (al emitir uno nuevo o al usar uno). */
    @Modifying
    @Query("update UserToken t set t.usedAt = :now where t.user.id = :userId and t.type = :type and t.usedAt is null")
    int invalidatePending(@Param("userId") Long userId, @Param("type") UserTokenType type, @Param("now") LocalDateTime now);

    @Modifying
    @Query("delete from UserToken t where t.expiresAt < :before")
    int deleteExpiredBefore(@Param("before") LocalDateTime before);
}
