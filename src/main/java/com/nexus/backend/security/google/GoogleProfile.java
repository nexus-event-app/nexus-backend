package com.nexus.backend.security.google;

/**
 * Lo que Google nos cuenta de la persona: el identificador estable de su cuenta, el email (y si Google lo ha
 * verificado) y el nombre. No se pide ni se guarda nada más (ni la foto).
 */
public record GoogleProfile(String subject, String email, boolean emailVerified, String name) {
}
