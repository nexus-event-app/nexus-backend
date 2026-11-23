package com.nexus.backend.moderation;

import com.nexus.backend.moderation.ContentFilter.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ContentFilterTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "¿Qué reviso antes de mi primer pull request?",
            "La disputa entre React y Vue no tiene fin",
            "Mi cónyuge también programa en Java",
            "El tren va retrasado y llego tarde a la charla",
            "Compré un cono de helado después del bootcamp",
            "Fui a la bolera con el equipo",
            "Scunthorpe United, Essex y Sussex",
            "Hice un curso de ES6 y HTML5 en la web3",
            "Ganas de aprender Python y de hablar con gente de Mongolia"
    })
    void dejaPasarElTextoNormal(String text) {
        assertThat(ContentFilter.check(text).isClean()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Qué mierda de documentación",
            "Joder, no me compila",
            "This is such bullshit",
            "Quina merda de servidor",
            "Me cago en el CSS",
            "JODEEER otra vez"
    })
    void retieneLasPalabrotasLeves(String text) {
        assertThat(ContentFilter.check(text).level()).isEqualTo(Level.HOLD);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Eres un maricón",
            "Vete a tu país, sudaca",
            "Calla, subnormal",
            "Ets un fill de puta",
            "You are a faggot",
            "Te voy a matar",
            "Eres un hijo de puta",
            "Feminazis por todas partes"
    })
    void bloqueaLosInsultosGraves(String text) {
        assertThat(ContentFilter.check(text).level()).isEqualTo(Level.BLOCK);
    }

    @Test
    void detectaLosTrucosHabituales() {
        assertThat(ContentFilter.check("m1erd4").level()).isEqualTo(Level.HOLD);
        assertThat(ContentFilter.check("p.u.t.o código").level()).isEqualTo(Level.HOLD);
        assertThat(ContentFilter.check("m a r i c o n").level()).isEqualTo(Level.BLOCK);
        assertThat(ContentFilter.check("SUBNORMAAAAL").level()).isEqualTo(Level.BLOCK);
    }

    @Test
    void loGraveManda() {
        var verdict = ContentFilter.check("Qué mierda", "eres un subnormal");
        assertThat(verdict.level()).isEqualTo(Level.BLOCK);
        assertThat(verdict.words()).containsExactly("subnormal");
    }

    @Test
    void recuerdaQuePalabrasHaEncontrado() {
        assertThat(ContentFilter.check("joder", null, "qué mierda").words()).containsExactly("joder", "mierda");
    }
}
