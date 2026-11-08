package com.nexus.backend.i18n;

import java.util.List;
import java.util.Locale;

/** Idiomas en los que responde el servidor. El castellano es el idioma por defecto y no lleva prefijo en las URLs. */
public final class Languages {

    public static final Locale DEFAULT = Locale.forLanguageTag("es");
    public static final List<Locale> SUPPORTED = List.of(DEFAULT, Locale.forLanguageTag("ca"), Locale.forLanguageTag("en"));

    private Languages() {
    }

    /** Prefijo de ruta del idioma en el frontend: vacío para el castellano, «/ca» o «/en» para el resto. */
    public static String pathPrefix(Locale locale) {
        String language = locale == null ? DEFAULT.getLanguage() : locale.getLanguage();
        return DEFAULT.getLanguage().equals(language) || SUPPORTED.stream().noneMatch(l -> l.getLanguage().equals(language))
                ? ""
                : "/" + language;
    }
}
