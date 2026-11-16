package com.nexus.backend.security;

import com.nexus.backend.exception.BadRequestException;

import java.nio.charset.StandardCharsets;

/**
 * Reglas de la contraseña que no puede comprobar la validación del formulario. BCrypt solo usa los primeros 72 bytes:
 * con tildes o emojis, 72 caracteres pueden ocupar más, y el resto se ignoraría sin avisar.
 */
public final class PasswordPolicy {

    static final int BCRYPT_MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    /** Lanza BadRequestException si la contraseña no cabe entera en BCrypt. */
    public static void requireBcryptLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new BadRequestException("error.password.tooLong");
        }
    }
}
