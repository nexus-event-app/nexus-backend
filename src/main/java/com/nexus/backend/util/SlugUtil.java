package com.nexus.backend.util;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtil {

    private static final int MAX_LENGTH = 80;
    private static final String FALLBACK = "sin-titulo";

    private SlugUtil() {
    }

    public static String slugify(String input) {
        if (input == null) {
            return FALLBACK;
        }
        String withoutAccents = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        String slug = withoutAccents.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (slug.length() > MAX_LENGTH) {
            slug = slug.substring(0, MAX_LENGTH).replaceAll("-+$", "");
        }
        return slug.isEmpty() ? FALLBACK : slug;
    }

    public static String idSlug(Long id, String title) {
        return id + "-" + slugify(title);
    }
}
