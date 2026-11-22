package com.nexus.backend.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class Messages {

    private final MessageSource messageSource;

    public String get(String codeOrText, Object... args) {
        return get(LocaleContextHolder.getLocale(), codeOrText, args);
    }

    public String get(Locale locale, String codeOrText, Object... args) {
        if (codeOrText == null) {
            return null;
        }
        return messageSource.getMessage(codeOrText, args, codeOrText, locale);
    }
}
