package com.nexus.backend.importer.provider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Identificadores externos estables y de longitud acotada ({@code prefijo:valor}). */
final class ExternalIds {

    private static final int MAX_LENGTH = 200;

    private ExternalIds() {
    }

    static String of(String prefix, String value) {
        String id = prefix + ":" + value;
        return id.length() <= MAX_LENGTH ? id : prefix + ":" + sha256(value);
    }

    static String hashed(String prefix, String value) {
        return prefix + ":" + sha256(value);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
