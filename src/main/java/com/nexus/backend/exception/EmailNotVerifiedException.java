package com.nexus.backend.exception;

/** La acción requiere haber verificado el email. Se responde 403 con el código EMAIL_NOT_VERIFIED. */
public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException() {
        super("error.email.notVerified");
    }
}
