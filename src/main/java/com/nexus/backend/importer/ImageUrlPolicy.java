package com.nexus.backend.importer;

import java.net.URI;
import java.util.List;
import java.util.Locale;

/**
 * Qué imágenes externas se aceptan como portada de una noticia importada. Solo https, con longitud razonable y que no
 * sean de servicios cuyas portadas son tarjetas de texto generadas automáticamente (DEV publica como portada una
 * tarjeta con el extracto del artículo cuando el autor no sube una). En esos casos la web muestra sus propias fotos.
 *
 * <p>Es una regla del código, no un ajuste de la fuente: se aplica siempre, a cualquier fuente.
 */
public final class ImageUrlPolicy {

    static final int MAX_URL_LENGTH = 500;

    /** Dominios (y sus subdominios) de imágenes que no se usan como portada. */
    private static final List<String> BLOCKED_DOMAINS = List.of("dev.to", "forem.com");

    /** Fragmentos de host de almacenamientos asociados a esos servicios (p. ej. dev-to-uploads.s3...). */
    private static final List<String> BLOCKED_HOST_PARTS = List.of("dev-to-uploads");

    private ImageUrlPolicy() {
    }

    public static boolean isAcceptable(String url) {
        if (url == null || url.length() > MAX_URL_LENGTH || !url.startsWith("https://")) {
            return false;
        }
        String host;
        try {
            host = URI.create(url.replace(" ", "%20")).getHost();
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (host == null) {
            return false;
        }
        String normalized = host.toLowerCase(Locale.ROOT);
        boolean blockedDomain = BLOCKED_DOMAINS.stream()
                .anyMatch(domain -> normalized.equals(domain) || normalized.endsWith("." + domain));
        boolean blockedPart = BLOCKED_HOST_PARTS.stream().anyMatch(normalized::contains);
        return !blockedDomain && !blockedPart;
    }
}
