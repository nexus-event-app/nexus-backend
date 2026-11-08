package com.nexus.backend.mail;

import com.nexus.backend.i18n.Languages;
import com.nexus.backend.i18n.Messages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.Locale;

/**
 * Emails de cuenta, en texto plano y HTML sencillo compatible con los clientes de correo. Se redactan en el idioma de la
 * petición que los provoca (cabecera Accept-Language) y los enlaces apuntan a la versión de la web en ese idioma.
 */
@Component
public class EmailTemplates {

    private final Messages messages;
    private final String publicUrl;

    public EmailTemplates(Messages messages, @Value("${app.public-url:http://localhost:5173}") String publicUrl) {
        this.messages = messages;
        this.publicUrl = publicUrl.replaceAll("/+$", "");
    }

    public EmailMessage emailVerification(String to, String name, String token) {
        Locale locale = LocaleContextHolder.getLocale();
        String brand = messages.get(locale, "mail.brand");
        return build(locale, to, messages.get(locale, "mail.verify.subject", brand), name,
                messages.get(locale, "mail.verify.intro", brand),
                messages.get(locale, "mail.verify.action"),
                link(locale, "/verificar-email", token),
                messages.get(locale, "mail.verify.note"));
    }

    public EmailMessage passwordReset(String to, String name, String token) {
        Locale locale = LocaleContextHolder.getLocale();
        String brand = messages.get(locale, "mail.brand");
        return build(locale, to, messages.get(locale, "mail.reset.subject", brand), name,
                messages.get(locale, "mail.reset.intro"),
                messages.get(locale, "mail.reset.action"),
                link(locale, "/restablecer-contrasena", token),
                messages.get(locale, "mail.reset.note"));
    }

    private String link(Locale locale, String path, String token) {
        return publicUrl + Languages.pathPrefix(locale) + path + "?token=" + token;
    }

    private EmailMessage build(Locale locale, String to, String subject, String name, String intro, String action,
                               String link, String note) {
        String brand = messages.get(locale, "mail.brand");
        String greeting = messages.get(locale, "mail.greeting", name);
        String signature = messages.get(locale, "mail.signature", brand);
        String fallback = messages.get(locale, "mail.fallbackLink");
        String text = greeting + "\n\n" + intro + "\n\n" + action + ": " + link + "\n\n" + note + "\n\n" + signature + "\n";

        String safeLink = HtmlUtils.htmlEscape(link);
        String html = """
                <!doctype html>
                <html lang="%s"><body style="margin:0;padding:24px;background:#f8fafc;font-family:Arial,Helvetica,sans-serif;color:#0f172a">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"><tr><td align="center">
                <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="max-width:560px;background:#ffffff;border-radius:12px;padding:32px">
                <tr><td>
                <p style="margin:0 0 24px;font-size:20px;font-weight:bold;color:#4f46e5">%s</p>
                <p style="margin:0 0 16px;font-size:16px">%s</p>
                <p style="margin:0 0 24px;font-size:16px;line-height:1.5">%s</p>
                <p style="margin:0 0 24px"><a href="%s" style="display:inline-block;background:#4f46e5;color:#ffffff;text-decoration:none;font-weight:bold;padding:12px 20px;border-radius:8px">%s</a></p>
                <p style="margin:0 0 16px;font-size:13px;color:#475569;line-height:1.5">%s<br><a href="%s" style="color:#4f46e5;word-break:break-all">%s</a></p>
                <p style="margin:0;font-size:13px;color:#475569;line-height:1.5">%s</p>
                </td></tr></table>
                </td></tr></table>
                </body></html>
                """.formatted(locale.getLanguage(), brand, HtmlUtils.htmlEscape(greeting), HtmlUtils.htmlEscape(intro), safeLink,
                HtmlUtils.htmlEscape(action), HtmlUtils.htmlEscape(fallback), safeLink, safeLink, HtmlUtils.htmlEscape(note));
        return new EmailMessage(to, subject, text, html);
    }
}
