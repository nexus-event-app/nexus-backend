package com.nexus.backend.importer;

import java.net.URI;
import java.util.List;
import java.util.Locale;

public final class ImageUrlPolicy {

    static final int MAX_URL_LENGTH = 500;

    private static final List<String> BLOCKED_DOMAINS = List.of("dev.to", "forem.com");

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
