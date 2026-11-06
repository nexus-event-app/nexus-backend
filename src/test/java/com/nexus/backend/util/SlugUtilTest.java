package com.nexus.backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlugUtilTest {

    @Test
    void removesAccentsAndSymbols() {
        assertEquals("como-conseguir-tu-primer-trabajo-en-it", SlugUtil.slugify("¿Cómo conseguir tu primer trabajo en IT?"));
    }

    @Test
    void collapsesSeparatorsAndTrimsEdges() {
        assertEquals("java-spring-boot", SlugUtil.slugify("  --Java   &  Spring__Boot--  "));
    }

    @Test
    void fallsBackWhenEmpty() {
        assertEquals("sin-titulo", SlugUtil.slugify("???"));
        assertEquals("sin-titulo", SlugUtil.slugify(null));
    }

    @Test
    void limitsLengthTo80() {
        String slug = SlugUtil.slugify("a".repeat(200));
        assertEquals(80, slug.length());
    }

    @Test
    void idSlugPrefixesId() {
        assertTrue(SlugUtil.idSlug(42L, "Hola Mundo").startsWith("42-hola-mundo"));
    }
}
