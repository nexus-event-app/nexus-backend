package com.nexus.backend.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

import java.util.Optional;
import java.util.Set;

/**
 * Convierte HTML de fuentes externas en texto plano legible (párrafos, listas y saltos de línea),
 * que es el único formato que guarda y muestra la aplicación.
 */
public final class HtmlText {

    private static final Set<String> BLOCK_TAGS = Set.of(
            "p", "div", "section", "article", "header", "footer", "blockquote", "pre", "table", "tr",
            "ul", "ol", "h1", "h2", "h3", "h4", "h5", "h6", "hr");
    private static final Set<String> SKIPPED_TAGS = Set.of("script", "style", "noscript", "iframe", "svg", "figure");

    private HtmlText() {
    }

    public static String toPlainText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        Element body = Jsoup.parseBodyFragment(html).body();
        body.select(String.join(",", SKIPPED_TAGS)).remove();

        StringBuilder out = new StringBuilder(html.length() / 2);
        body.traverse(new NodeVisitor() {
            // Justo después de una viñeta, los bloques que abre el punto (un <p> o <div> dentro del <li>) no añaden
            // saltos de línea: si no, la viñeta quedaría sola en una línea y su texto en la siguiente.
            private boolean afterBullet;

            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode text) {
                    if (!text.isBlank()) {
                        afterBullet = false;
                    }
                    out.append(text.text());
                } else if (node instanceof Element element) {
                    String tag = element.normalName();
                    if (tag.equals("br")) {
                        out.append('\n');
                    } else if (tag.equals("li")) {
                        out.append("\n• ");
                        afterBullet = true;
                    } else if (BLOCK_TAGS.contains(tag) && !afterBullet) {
                        // Dentro de un punto, un segundo bloque va en la línea siguiente, sin línea en blanco.
                        out.append(insideListItem(element) ? "\n" : "\n\n");
                    }
                }
            }

            @Override
            public void tail(Node node, int depth) {
                // Al cerrar un bloque dentro de un punto no se deja línea en blanco: el siguiente punto ya empieza en
                // su propia línea.
                if (node instanceof Element element && BLOCK_TAGS.contains(element.normalName())
                        && !insideListItem(element)) {
                    out.append("\n\n");
                }
            }
        });
        String text = out.toString();
        return normalize(text);
    }

    private static boolean insideListItem(Element element) {
        return element.parents().stream().anyMatch(parent -> parent.normalName().equals("li"));
    }

    /** Primera imagen https del HTML, si la hay. */
    public static Optional<String> firstImage(String html) {
        if (html == null || html.isBlank()) {
            return Optional.empty();
        }
        return Jsoup.parseBodyFragment(html).select("img[src]").stream()
                .map(img -> img.attr("src").trim())
                .filter(src -> src.startsWith("https://"))
                .findFirst();
    }

    private static String normalize(String text) {
        String cleaned = TextUtil.clean(text.replace(' ', ' '));
        return cleaned
                .replaceAll("[ \\t]+", " ")
                .replaceAll(" *\\n *", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
