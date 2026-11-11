package com.nexus.backend.util;

import java.util.Set;

/**
 * Idioma del contenido importado o publicado (no el de la interfaz): castellano o inglés. Las noticias en inglés se
 * marcan en la web y el lector de pantalla las pronuncia en inglés.
 */
public final class ContentLanguage {

    public static final String SPANISH = "es";
    public static final String ENGLISH = "en";
    public static final String PATTERN = "es|en";
    private static final Set<String> SUPPORTED = Set.of(SPANISH, ENGLISH);

    private ContentLanguage() {
    }

    /** Idioma válido o castellano si no se indica o no es uno de los admitidos. */
    public static String orDefault(String language) {
        return language != null && SUPPORTED.contains(language) ? language : SPANISH;
    }

    /** Idioma válido o null (sin filtro) si no se indica o no es uno de los admitidos. */
    public static String orNull(String language) {
        return language != null && SUPPORTED.contains(language) ? language : null;
    }
}
