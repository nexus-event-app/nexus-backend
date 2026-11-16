package com.nexus.backend.security.google;

/** Qué pasa al volver de Google. La web recibe solo un código de un solo uso, nunca el token de sesión en la URL. */
public sealed interface GoogleLoginOutcome {

    /** Cuenta ya existente (o recién vinculada): la web canjea el código por la sesión. */
    record SignedIn(String code) implements GoogleLoginOutcome {
    }

    /** Persona nueva: falta aceptar las condiciones en «Ya casi está» antes de crear la cuenta. */
    record SignupPending(String token) implements GoogleLoginOutcome {
    }

    /** No se puede entrar: cuenta desactivada o email sin verificar en Google. */
    record Rejected(String reason) implements GoogleLoginOutcome {
    }
}
