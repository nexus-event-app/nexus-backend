package com.nexus.backend.security;

import com.nexus.backend.exception.BadRequestException;

import java.nio.charset.StandardCharsets;

public final class PasswordPolicy {

    static final int BCRYPT_MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    public static void requireBcryptLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new BadRequestException("error.password.tooLong");
        }
    }
}
