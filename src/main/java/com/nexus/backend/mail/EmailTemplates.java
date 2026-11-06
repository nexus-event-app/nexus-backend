package com.nexus.backend.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/** Textos de los emails de cuenta, en texto plano y HTML sencillo compatible con los clientes de correo. */
@Component
public class EmailTemplates {

    private static final String BRAND = "Primer Commit";

    private final String publicUrl;

    public EmailTemplates(@Value("${app.public-url:http://localhost:5173}") String publicUrl) {
        this.publicUrl = publicUrl.replaceAll("/+$", "");
    }

    public EmailMessage emailVerification(String to, String name, String token) {
        String link = publicUrl + "/verificar-email?token=" + token;
        return build(to, "Confirma tu email en " + BRAND, name,
                "Gracias por unirte a " + BRAND + ". Confirma tu email para poder participar en el foro y compartir tu testimonio.",
                "Confirmar mi email", link,
                "El enlace caduca en 48 horas. Si no has creado una cuenta, ignora este mensaje.");
    }

    public EmailMessage passwordReset(String to, String name, String token) {
        String link = publicUrl + "/restablecer-contrasena?token=" + token;
        return build(to, "Restablece tu contraseña de " + BRAND, name,
                "Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.",
                "Elegir una contraseña nueva", link,
                "El enlace caduca en 1 hora y solo se puede usar una vez. Si no lo has pedido tú, ignora este mensaje: "
                        + "tu contraseña no cambiará.");
    }

    private EmailMessage build(String to, String subject, String name, String intro, String action, String link, String note) {
        String greeting = "Hola, " + name + ":";
        String text = greeting + "\n\n" + intro + "\n\n" + action + ": " + link + "\n\n" + note + "\n\n— El equipo de " + BRAND + "\n";

        String safeLink = HtmlUtils.htmlEscape(link);
        String html = """
                <!doctype html>
                <html lang="es"><body style="margin:0;padding:24px;background:#f8fafc;font-family:Arial,Helvetica,sans-serif;color:#0f172a">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"><tr><td align="center">
                <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="max-width:560px;background:#ffffff;border-radius:12px;padding:32px">
                <tr><td>
                <p style="margin:0 0 24px;font-size:20px;font-weight:bold;color:#4f46e5">%s</p>
                <p style="margin:0 0 16px;font-size:16px">%s</p>
                <p style="margin:0 0 24px;font-size:16px;line-height:1.5">%s</p>
                <p style="margin:0 0 24px"><a href="%s" style="display:inline-block;background:#4f46e5;color:#ffffff;text-decoration:none;font-weight:bold;padding:12px 20px;border-radius:8px">%s</a></p>
                <p style="margin:0 0 16px;font-size:13px;color:#475569;line-height:1.5">Si el botón no funciona, copia este enlace en tu navegador:<br><a href="%s" style="color:#4f46e5;word-break:break-all">%s</a></p>
                <p style="margin:0;font-size:13px;color:#475569;line-height:1.5">%s</p>
                </td></tr></table>
                </td></tr></table>
                </body></html>
                """.formatted(BRAND, HtmlUtils.htmlEscape(greeting), HtmlUtils.htmlEscape(intro), safeLink,
                HtmlUtils.htmlEscape(action), safeLink, safeLink, HtmlUtils.htmlEscape(note));
        return new EmailMessage(to, subject, text, html);
    }
}
