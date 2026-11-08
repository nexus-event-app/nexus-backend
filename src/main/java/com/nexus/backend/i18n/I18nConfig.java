package com.nexus.backend.i18n;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * El idioma de cada petición sale de la cabecera Accept-Language, que envía el frontend con el idioma elegido.
 * Si falta o no es uno de los soportados, se responde en castellano.
 */
@Configuration
public class I18nConfig {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(Languages.SUPPORTED);
        resolver.setDefaultLocale(Languages.DEFAULT);
        return resolver;
    }
}
