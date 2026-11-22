package com.nexus.backend.util;

import java.util.Set;

public final class ContentLanguage {

    public static final String SPANISH = "es";
    public static final String ENGLISH = "en";
    public static final String PATTERN = "es|en";
    private static final Set<String> SUPPORTED = Set.of(SPANISH, ENGLISH);

    private ContentLanguage() {
    }

    public static String orDefault(String language) {
        return language != null && SUPPORTED.contains(language) ? language : SPANISH;
    }

    public static String orNull(String language) {
        return language != null && SUPPORTED.contains(language) ? language : null;
    }
}
