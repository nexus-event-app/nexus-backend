package com.nexus.backend.security.google;

public record GoogleProfile(String subject, String email, boolean emailVerified, String name) {
}
