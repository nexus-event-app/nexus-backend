package com.nexus.backend.util;

import java.util.regex.Pattern;

public final class BulletText {

    private static final Pattern LONELY_BULLET = Pattern.compile("•[ \\t]*\\n+[ \\t]*(?=\\S)");
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
