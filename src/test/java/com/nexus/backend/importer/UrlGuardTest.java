package com.nexus.backend.importer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlGuardTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "file:///etc/passwd", "ftp://example.com/feed", "http://localhost:8080/api", "http://127.0.0.1/feed",
            "http://10.0.0.5/feed", "http://192.168.1.10/feed", "http://169.254.169.254/latest/meta-data",
            "http://[::1]/feed", "http://100.64.0.1/feed", "no es una url"})
    void rejectsUnsafeUrls(String url) {
        assertThatThrownBy(() -> UrlGuard.validate(url)).isInstanceOf(ImportException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://www.freecodecamp.org/espanol/news/rss/", "http://example.com/feed.xml"})
    void acceptsPublicUrls(String url) {
        assertThatCode(() -> UrlGuard.validate(url)).doesNotThrowAnyException();
    }
}
