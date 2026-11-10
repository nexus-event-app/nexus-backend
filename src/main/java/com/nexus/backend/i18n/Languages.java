package com.nexus.backend.i18n;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Idiomas en los que responde el servidor y cómo son las URLs del frontend en cada uno. El castellano es el idioma
 * por defecto: no lleva prefijo y sus rutas son las internas («/noticias»). En catalán e inglés la ruta lleva prefijo
 * y la sección traducida («/ca/noticies», «/en/news»).
 */
public final class Languages {

    public static final Locale DEFAULT = Locale.forLanguageTag("es");
    public static final List<Locale> SUPPORTED = List.of(DEFAULT, Locale.forLanguageTag("ca"), Locale.forLanguageTag("en"));

    /**
     * Nombre de cada sección en catalán y en inglés. Debe coincidir con src/shared/i18n/routeSegments.js del
     * frontend; LanguagesTest lo comprueba.
     */
    static final Map<String, Map<String, String>> SEGMENTS = Map.ofEntries(
            Map.entry("noticias", Map.of("ca", "noticies", "en", "news")),
            Map.entry("empleo", Map.of("ca", "feina", "en", "jobs")),
            Map.entry("foro", Map.of("ca", "forum", "en", "forum")),
            Map.entry("testimonios", Map.of("ca", "testimonis", "en", "testimonials")),
            Map.entry("aviso-legal", Map.of("ca", "avis-legal", "en", "legal-notice")),
            Map.entry("privacidad", Map.of("ca", "privacitat", "en", "privacy")),
            Map.entry("cookies", Map.of("ca", "galetes", "en", "cookies")),
            Map.entry("login", Map.of("ca", "inicia-sessio", "en", "login")),
            Map.entry("registro", Map.of("ca", "registre", "en", "sign-up")),
            Map.entry("recuperar-contrasena", Map.of("ca", "recupera-contrasenya", "en", "forgot-password")),
            Map.entry("restablecer-contrasena", Map.of("ca", "restableix-contrasenya", "en", "reset-password")),
            Map.entry("verificar-email", Map.of("ca", "verifica-correu", "en", "verify-email")),
            Map.entry("perfil", Map.of("ca", "perfil", "en", "profile")),
            Map.entry("buscar", Map.of("ca", "cerca", "en", "search")),
            Map.entry("sobre-el-proyecto", Map.of("ca", "sobre-el-projecte", "en", "about")));

    private Languages() {
    }

    /** Prefijo de ruta del idioma en el frontend: vacío para el castellano, «/ca» o «/en» para el resto. */
    public static String pathPrefix(Locale locale) {
        String language = languageOf(locale);
        return DEFAULT.getLanguage().equals(language) ? "" : "/" + language;
    }

    /**
     * Ruta del frontend en el idioma indicado a partir de la ruta interna en castellano:
     * {@code localizePath(en, "/noticias/12-x")} devuelve {@code "/en/news/12-x"}. La portada es «/», «/ca» o «/en».
     */
    public static String localizePath(Locale locale, String internalPath) {
        String language = languageOf(locale);
        String prefix = pathPrefix(locale);
        if ("/".equals(internalPath)) {
            return prefix.isEmpty() ? "/" : prefix;
        }
        if (prefix.isEmpty()) {
            return internalPath;
        }
        int end = internalPath.indexOf('/', 1);
        String first = end == -1 ? internalPath.substring(1) : internalPath.substring(1, end);
        String rest = end == -1 ? "" : internalPath.substring(end);
        String translated = SEGMENTS.getOrDefault(first, Map.of()).getOrDefault(language, first);
        return prefix + "/" + translated + rest;
    }

    private static String languageOf(Locale locale) {
        String language = locale == null ? DEFAULT.getLanguage() : locale.getLanguage();
        return SUPPORTED.stream().anyMatch(l -> l.getLanguage().equals(language)) ? language : DEFAULT.getLanguage();
    }
}
