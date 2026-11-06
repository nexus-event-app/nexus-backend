package com.nexus.backend.importer;

/** Error al obtener o interpretar una fuente externa. El mensaje se muestra al admin. */
public class ImportException extends RuntimeException {

    public ImportException(String message) {
        super(message);
    }

    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
