package com.nexus.backend.i18n;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Languages {

    public static final Locale DEFAULT = Locale.forLanguageTag("es");
    public static final List<Locale> SUPPORTED = List.of(DEFAULT, Locale.forLanguageTag("ca"), Locale.forLanguageTag("en"));

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
            Map.entry("sobre-el-proyecto", Map.of("ca", "sobre-el-projecte", "en", "about")),
            Map.entry("notificaciones", Map.of("ca", "notificacions", "en", "notifications")),
            Map.entry("rutas", Map.of("ca", "rutes", "en", "learning-paths")),
            Map.entry("glosario", Map.of("ca", "glossari", "en", "glossary")),
            Map.entry("eventos", Map.of("ca", "esdeveniments", "en", "events")));

    static final Map<String, Map<String, Map<String, String>>> SUBSEGMENTS = Map.of(
            "foro", Map.of("nueva", Map.of("ca", "nova", "en", "new")),
            "empleo", Map.of("guardadas", Map.of("ca", "desades", "en", "saved")),
            "perfil", Map.of("editar", Map.of("ca", "edita", "en", "edit")),
            "eventos", Map.of("nuevo", Map.of("ca", "nou", "en", "new")),
            "rutas", Map.of(
                    "datos", Map.of("ca", "dades", "en", "data"),
                    "ia", Map.of("ca", "ia", "en", "ai")));

    private Languages() {
    }

    public static String pathPrefix(Locale locale) {
        String language = languageOf(locale);
        return DEFAULT.getLanguage().equals(language) ? "" : "/" + language;
    }

    public static String localizePath(Locale locale, String internalPath) {
        String language = languageOf(locale);
        String prefix = pathPrefix(locale);
        if ("/".equals(internalPath)) {
            return prefix.isEmpty() ? "/" : prefix;
        }
        if (prefix.isEmpty()) {
            return internalPath;
        }
        String[] parts = internalPath.substring(1).split("/", 3);
        String first = parts[0];
        StringBuilder localized = new StringBuilder(prefix).append('/')
                .append(SEGMENTS.getOrDefault(first, Map.of()).getOrDefault(language, first));
        if (parts.length > 1) {
            String second = parts[1];
            localized.append('/').append(SUBSEGMENTS.getOrDefault(first, Map.of())
                    .getOrDefault(second, Map.of()).getOrDefault(language, second));
        }
        if (parts.length > 2) {
            localized.append('/').append(parts[2]);
        }
        return localized.toString();
    }

    private static String languageOf(Locale locale) {
        String language = locale == null ? DEFAULT.getLanguage() : locale.getLanguage();
        return SUPPORTED.stream().anyMatch(l -> l.getLanguage().equals(language)) ? language : DEFAULT.getLanguage();
    }
}
