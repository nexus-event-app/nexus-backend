package com.nexus.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "unit-test-secret-with-more-than-thirty-two-bytes";

    private MockEnvironment environmentWithProfile(String profile) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profile);
        return environment;
    }

    @Test
    void generatesAndValidatesToken() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000L, environmentWithProfile("prod"));

        String token = jwtUtil.generateToken("ana@example.com", "USER", 1L);

        assertEquals(Optional.of("ana@example.com"), jwtUtil.extractEmailIfValid(token));
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000L, environmentWithProfile("prod"));
        JwtUtil other = new JwtUtil("another-secret-with-more-than-thirty-two-bytes", 60_000L, environmentWithProfile("prod"));

        String foreignToken = other.generateToken("ana@example.com", "USER", 1L);

        assertTrue(jwtUtil.extractEmailIfValid(foreignToken).isEmpty());
        assertTrue(jwtUtil.extractEmailIfValid("no-es-un-token").isEmpty());
    }

    @Test
    void rejectsExpiredToken() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, -1_000L, environmentWithProfile("prod"));

        String token = jwtUtil.generateToken("ana@example.com", "USER", 1L);

        assertTrue(jwtUtil.extractEmailIfValid(token).isEmpty());
    }

    @Test
    void failsWithoutSecretOutsideDevAndTest() {
        assertThrows(IllegalStateException.class, () -> new JwtUtil("", 60_000L, environmentWithProfile("prod")));
    }

    @Test
    void generatesRandomSecretInDev() {
        JwtUtil jwtUtil = new JwtUtil("", 60_000L, environmentWithProfile("dev"));

        String token = jwtUtil.generateToken("ana@example.com", "USER", 1L);

        assertTrue(jwtUtil.extractEmailIfValid(token).isPresent());
    }

    @Test
    void rejectsShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtUtil("corto", 60_000L, environmentWithProfile("dev")));
    }
}
