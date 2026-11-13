package com.nexus.backend.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlTextTest {

    @Test
    void keepsParagraphsAndListsAsPlainText() {
        String html = "<p>Hola <strong>mundo</strong>.</p><ul><li>React</li><li>CSS &amp; HTML</li></ul><p>Fin<br>línea</p>";
        assertThat(HtmlText.toPlainText(html)).isEqualTo("Hola mundo.\n\n• React\n• CSS & HTML\n\nFin\nlínea");
    }

    @Test
    void keepsTheBulletAndItsTextTogetherWhenListItemsWrapParagraphs() {
        // Así llegan muchas ofertas importadas: cada punto con un párrafo dentro.
        String html = "<p>What you will do</p><ul><li><p>Plan usability tests.</p></li><li><div>Run interviews.</div></li></ul>";
        assertThat(HtmlText.toPlainText(html))
                .isEqualTo("What you will do\n\n• Plan usability tests.\n• Run interviews.");
    }

    @Test
    void dropsScriptsAndMarkup() {
        String html = "<div>Texto<script>alert(1)</script><style>p{}</style> <img src=x onerror=alert(1)></div>";
        assertThat(HtmlText.toPlainText(html)).isEqualTo("Texto");
    }

    @Test
    void handlesEmptyInput() {
        assertThat(HtmlText.toPlainText(null)).isEmpty();
        assertThat(HtmlText.toPlainText("   ")).isEmpty();
    }

    @Test
    void findsFirstSecureImage() {
        assertThat(HtmlText.firstImage("<img src='http://a/x.png'><img src='https://b/y.png'>")).contains("https://b/y.png");
        assertThat(HtmlText.firstImage("<p>sin imagen</p>")).isEmpty();
    }

    @Test
    void truncatesOnWordBoundary() {
        assertThat(TextUtil.truncate("uno dos tres cuatro", 12)).isEqualTo("uno dos…");
        assertThat(TextUtil.truncate("corto", 12)).isEqualTo("corto");
    }
}
