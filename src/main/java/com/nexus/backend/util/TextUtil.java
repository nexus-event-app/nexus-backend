package com.nexus.backend.util;

public final class TextUtil {

    private TextUtil() {
    }

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

    public static String cleanLine(String value) {
        String cleaned = clean(value);
        return cleaned == null ? null : cleaned.replaceAll("\\s+", " ");
    }

    public static String optionalLine(String value) {
        String cleaned = cleanLine(value);
        return cleaned == null || cleaned.isEmpty() ? null : cleaned;
    }

    public static String optional(String value) {
        String cleaned = clean(value);
        return cleaned == null || cleaned.isEmpty() ? null : cleaned;
    }

    public static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        String cut = value.substring(0, maxLength - 1);
        int lastSpace = cut.lastIndexOf(' ');
        if (lastSpace > maxLength / 2) {
            cut = cut.substring(0, lastSpace);
        }
        return cut.stripTrailing() + "…";
    }
}
