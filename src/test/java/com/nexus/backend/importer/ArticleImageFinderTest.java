package com.nexus.backend.importer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleImageFinderTest {

    private static final String PAGE = "https://blog.example.com/posts/hola";

    @Test
    void readsTheOpenGraphImage() {
        String html = "<html><head><meta property=\"og:image\" content=\"https://cdn.example.com/portada.jpg\"></head></html>";
        assertThat(ArticleImageFinder.extract(html, PAGE)).contains("https://cdn.example.com/portada.jpg");
    }

    @Test
    void resolvesRelativeAddressesAgainstThePage() {
        String html = "<head><meta name=\"twitter:image\" content=\"/img/portada 1.png\"></head>";
        assertThat(ArticleImageFinder.extract(html, PAGE)).contains("https://blog.example.com/img/portada%201.png");
    }

    @Test
    void skipsInsecureImagesAndFallsBackToTheNextTag() {
        String html = "<head><meta property=\"og:image\" content=\"http://insegura.example.com/a.jpg\">"
                + "<meta name=\"twitter:image\" content=\"https://segura.example.com/b.jpg\"></head>";
        assertThat(ArticleImageFinder.extract(html, PAGE)).contains("https://segura.example.com/b.jpg");
    }

    @Test
    void returnsEmptyWhenThePageHasNoImage() {
        assertThat(ArticleImageFinder.extract("<html><head><title>Sin imagen</title></head></html>", PAGE)).isEmpty();
        assertThat(ArticleImageFinder.extract("<meta property=\"og:image\" content=\"javascript:alert(1)\">", PAGE)).isEmpty();
    }
}
