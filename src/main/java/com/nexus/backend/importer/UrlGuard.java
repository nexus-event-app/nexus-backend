package com.nexus.backend.importer;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Valida las URLs de las fuentes externas: solo http/https y nunca direcciones internas
 * (loopback, red privada, link-local...), para que el servidor no pueda usarse contra su propia red.
 */
public final class UrlGuard {

    private UrlGuard() {
    }

    public static URI validate(String url) {
        URI uri;
        try {
            uri = new URI(url.trim());
        } catch (URISyntaxException | NullPointerException e) {
            throw new ImportException("La URL de la fuente no es válida");
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new ImportException("La URL de la fuente debe empezar por http:// o https://");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new ImportException("La URL de la fuente no tiene dominio");
        }
        if (host.equalsIgnoreCase("localhost") || host.toLowerCase(Locale.ROOT).endsWith(".localhost")) {
            throw new ImportException("La URL de la fuente apunta a una dirección interna");
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (isInternal(address)) {
                    throw new ImportException("La URL de la fuente apunta a una dirección interna");
                }
            }
        } catch (UnknownHostException e) {
            // Sin DNS local (p. ej. detrás de un proxy de salida): la resolución la hace el proxy.
        }
        return uri;
    }

    static boolean isInternal(InetAddress address) {
        if (address.isLoopbackAddress() || address.isAnyLocalAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return true;
        }
        byte[] bytes = address.getAddress();
        if (address instanceof Inet6Address) {
            return (bytes[0] & 0xFE) == 0xFC; // fc00::/7, direcciones locales únicas
        }
        int first = bytes[0] & 0xFF;
        int second = bytes[1] & 0xFF;
        return (first == 100 && second >= 64 && second <= 127) // 100.64.0.0/10, CGNAT
                || first == 0;
    }
}
