package com.nexus.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JwtUtil {

    static final int MIN_SECRET_BYTES = 32;
    private static final int GENERATED_SECRET_BYTES = 64;

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtUtil(@Value("${jwt.secret:}") String secret,
                   @Value("${jwt.expiration:86400000}") long expirationMillis,
                   Environment environment) {
        this.signingKey = buildKey(secret, environment.acceptsProfiles(Profiles.of("dev", "test")));
        this.expirationMillis = expirationMillis;
    }

    private static SecretKey buildKey(String secret, boolean allowGenerated) {
        if (secret == null || secret.isBlank()) {
            if (!allowGenerated) {
                throw new IllegalStateException(
                        "Falta la variable JWT_SECRET (jwt.secret). Define un secreto de al menos "
                                + MIN_SECRET_BYTES + " bytes para arrancar fuera de los perfiles dev/test.");
            }
            byte[] random = new byte[GENERATED_SECRET_BYTES];
            new SecureRandom().nextBytes(random);
            log.warn("JWT_SECRET no está definido: se ha generado un secreto aleatorio para este arranque. "
                    + "Los tokens dejarán de ser válidos al reiniciar.");
            return Keys.hmacShaKeyFor(random);
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET es demasiado corto: se requieren al menos " + MIN_SECRET_BYTES + " bytes.");
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(String email, String role, Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(signingKey)
                .compact();
    }

    /** Datos verificados de un token válido. */
    public record TokenClaims(String email, Instant issuedAt) {
    }

    /** Devuelve el email (subject) si el token es válido y no ha caducado; vacío en cualquier otro caso. */
    public Optional<String> extractEmailIfValid(String token) {
        return parse(token).map(TokenClaims::email);
    }

    /** Email y fecha de emisión si el token es válido y no ha caducado; vacío en cualquier otro caso. */
    public Optional<TokenClaims> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (claims.getSubject() == null) {
                return Optional.empty();
            }
            Instant issuedAt = claims.getIssuedAt() == null ? Instant.EPOCH : claims.getIssuedAt().toInstant();
            return Optional.of(new TokenClaims(claims.getSubject(), issuedAt));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
