package com.nexus.backend.exception;

public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException() {
        super("error.email.notVerified");
    }
}
