package com.nexus.backend.dto.response;

/** Formas de entrar disponibles además del email: la web solo muestra «Continuar con Google» si está configurado. */
public record AuthProvidersResponse(boolean google) {
}
