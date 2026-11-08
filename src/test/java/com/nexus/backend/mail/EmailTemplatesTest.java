package com.nexus.backend.mail;

import com.nexus.backend.i18n.Messages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/** Los emails salen en el idioma de la petición y sus enlaces apuntan a la versión de la web en ese idioma. */
class EmailTemplatesTest {

    private final EmailTemplates templates = new EmailTemplates(new Messages(messageSource()), "https://primercommit.test/");

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void spanishEmailsKeepTheRootUrls() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));

        EmailMessage message = templates.emailVerification("ana@test.com", "Ana", "TOKEN");

        assertThat(message.subject()).isEqualTo("Confirma tu email en Primer Commit");
        assertThat(message.text()).contains("Hola, Ana:").contains("https://primercommit.test/verificar-email?token=TOKEN");
        assertThat(message.html()).contains("<html lang=\"es\">");
    }

    @Test
    void catalanEmailsUseTheCatalanTextsAndPrefix() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("ca"));

        EmailMessage message = templates.passwordReset("ana@test.com", "Ana", "TOKEN");

        assertThat(message.subject()).isEqualTo("Restableix la teva contrasenya de Primer Commit");
        assertThat(message.text()).contains("https://primercommit.test/ca/restablecer-contrasena?token=TOKEN");
        assertThat(message.html()).contains("<html lang=\"ca\">");
    }

    @Test
    void englishEmailsUseTheEnglishTextsAndPrefix() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        EmailMessage message = templates.emailVerification("ana@test.com", "Ana", "TOKEN");

        assertThat(message.subject()).isEqualTo("Confirm your email on Primer Commit");
        assertThat(message.text()).contains("Hi Ana,").contains("https://primercommit.test/en/verificar-email?token=TOKEN");
    }

    private static ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        return source;
    }
}
