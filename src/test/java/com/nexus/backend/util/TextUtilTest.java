package com.nexus.backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TextUtilTest {

    @Test
    void keepsSymbolsAsPlainText() {
        assertEquals("C++ & Java <3", TextUtil.clean("  C++ & Java <3  "));
    }

    @Test
    void normalizesLineBreaksAndStripsControlCharacters() {
        assertEquals("a\nb\tc", TextUtil.clean("a\r\nb\tc\u0000\u0007"));
    }

    @Test
    void cleanLineCollapsesWhitespace() {
        assertEquals("a b c", TextUtil.cleanLine(" a\n b \t c "));
    }

    @Test
    void optionalReturnsNullWhenBlank() {
        assertNull(TextUtil.optionalLine("   "));
        assertNull(TextUtil.optional(null));
    }
}
