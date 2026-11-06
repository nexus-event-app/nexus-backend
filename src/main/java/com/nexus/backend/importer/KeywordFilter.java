package com.nexus.backend.importer;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Filtro por palabras clave de una fuente: coincide si el texto contiene al menos una, como palabra
 * completa, sin distinguir mayúsculas ni acentos. Sin palabras clave, todo coincide.
 */
public final class KeywordFilter {

    private static final KeywordFilter ALL = new KeywordFilter(null);

    private final Pattern pattern;

    private KeywordFilter(Pattern pattern) {
        this.pattern = pattern;
    }

    public static KeywordFilter of(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            return ALL;
        }
        List<String> terms = Arrays.stream(keywords.split(","))
                .map(KeywordFilter::normalize)
                .map(String::trim)
                .filter(term -> !term.isEmpty())
                .map(Pattern::quote)
                .toList();
        if (terms.isEmpty()) {
            return ALL;
        }
        return new KeywordFilter(Pattern.compile("(?<![\\p{L}\\p{N}])(" + String.join("|", terms) + ")(?![\\p{L}\\p{N}])"));
    }

    public boolean matches(String text) {
        return pattern == null || pattern.matcher(normalize(text)).find();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
