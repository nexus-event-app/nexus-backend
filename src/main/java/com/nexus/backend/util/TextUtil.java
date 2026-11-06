package com.nexus.backend.util;

public final class TextUtil {

    private TextUtil() {
    }

    /**
     * Normaliza texto multilínea: saltos de línea unificados a \n, sin caracteres de control
     * (salvo \n y \t) y sin espacios en los extremos.
     */
    public static String clean(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace("\r\n", "\n").replace('\r', '\n');
        StringBuilder builder = new StringBuilder(normalized.length());
        normalized.codePoints().forEach(codePoint -> {
            if (codePoint == '\n' || codePoint == '\t' || !Character.isISOControl(codePoint)) {
                builder.appendCodePoint(codePoint);
            }
        });
        return builder.toString().trim();
    }

    /** Igual que {@link #clean(String)} pero en una sola línea (los espacios y saltos se colapsan). */
    public static String cleanLine(String value) {
        String cleaned = clean(value);
        return cleaned == null ? null : cleaned.replaceAll("\\s+", " ");
    }

    /** Texto de una línea; devuelve null si queda vacío. */
    public static String optionalLine(String value) {
        String cleaned = cleanLine(value);
        return cleaned == null || cleaned.isEmpty() ? null : cleaned;
    }

    /** Texto multilínea; devuelve null si queda vacío. */
    public static String optional(String value) {
        String cleaned = clean(value);
        return cleaned == null || cleaned.isEmpty() ? null : cleaned;
    }
}
