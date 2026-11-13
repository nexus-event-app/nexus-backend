package com.nexus.backend.util;

import java.util.regex.Pattern;

/**
 * Arregla las listas de los textos importados antes de que HtmlText supiera tratar los puntos con un párrafo dentro:
 * la viñeta quedaba sola en una línea («•») y su texto unas líneas más abajo.
 */
public final class BulletText {

    /** Una viñeta sola, seguida de saltos de línea antes de su texto. */
    private static final Pattern LONELY_BULLET = Pattern.compile("•[ \\t]*\\n+[ \\t]*(?=\\S)");
    /** Líneas en blanco entre dos puntos seguidos de una lista. */
    private static final Pattern GAP_BETWEEN_BULLETS = Pattern.compile("(?m)^(• [^\\n]*)\\n{2,}(?=• )");

    private BulletText() {
    }

    public static String fix(String text) {
        if (text == null || text.indexOf('•') < 0) {
            return text;
        }
        String joined = LONELY_BULLET.matcher(text).replaceAll("• ");
        String previous;
        do {
            previous = joined;
            joined = GAP_BETWEEN_BULLETS.matcher(joined).replaceAll("$1\n");
        } while (!joined.equals(previous));
        return joined;
    }
}
