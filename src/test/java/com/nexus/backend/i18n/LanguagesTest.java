package com.nexus.backend.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LanguagesTest {

    private static final Locale ES = Locale.forLanguageTag("es");
    private static final Locale CA = Locale.forLanguageTag("ca");
    private static final Locale EN = Locale.forLanguageTag("en");

    @Test
    void localizesSectionsAndKeepsIdentifiers() {
        assertThat(Languages.localizePath(ES, "/noticias/12-hola")).isEqualTo("/noticias/12-hola");
        assertThat(Languages.localizePath(CA, "/noticias/12-hola")).isEqualTo("/ca/noticies/12-hola");
        assertThat(Languages.localizePath(EN, "/noticias/12-hola")).isEqualTo("/en/news/12-hola");
        assertThat(Languages.localizePath(EN, "/empleo")).isEqualTo("/en/jobs");
        assertThat(Languages.localizePath(CA, "/verificar-email")).isEqualTo("/ca/verifica-correu");
        assertThat(Languages.localizePath(EN, "/restablecer-contrasena")).isEqualTo("/en/reset-password");
    }

    @Test
    void homePageAndUnknownLanguages() {
        assertThat(Languages.localizePath(ES, "/")).isEqualTo("/");
        assertThat(Languages.localizePath(CA, "/")).isEqualTo("/ca");
        assertThat(Languages.localizePath(Locale.FRENCH, "/noticias")).isEqualTo("/noticias");
        assertThat(Languages.localizePath(null, "/empleo")).isEqualTo("/empleo");
        assertThat(Languages.pathPrefix(Locale.GERMAN)).isEmpty();
    }

    /** Copia exacta de src/shared/i18n/routeSegments.js del frontend: si una cambia, la otra también. */
    @Test
    void matchesTheFrontendRouteTable() {
        assertThat(Languages.SEGMENTS).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
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
                Map.entry("notificaciones", Map.of("ca", "notificacions", "en", "notifications"))));
    }
}
