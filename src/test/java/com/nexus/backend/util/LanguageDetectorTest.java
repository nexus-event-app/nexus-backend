package com.nexus.backend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LanguageDetectorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "Cómo preparar tu primera entrevista técnica. Consejos para quien busca su primer empleo en el sector.",
            "GitHub lanza una nueva versión de Copilot para los equipos que trabajan con Python",
            "¿Qué es Docker y por qué deberías aprenderlo?",
            "Guía de JavaScript desde cero: variables, funciones y bucles explicados paso a paso",
            "La UE aprueba la ley de IA. Las empresas tendrán dos años para adaptarse."})
    void recognizesSpanish(String text) {
        assertThat(LanguageDetector.detect(text)).as(text).contains("es");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "How to prepare for your first technical interview. Tips for people looking for their first job.",
            "GitHub launches a new version of Copilot for teams that work with Python",
            "What is Docker and why should you learn it?",
            "I built my first React app in a weekend: here is what I learned",
            "The EU passes the AI Act. Companies will have two years to adapt."})
    void recognizesEnglish(String text) {
        assertThat(LanguageDetector.detect(text)).as(text).contains("en");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "JavaScript, TypeScript, React", "Kubernetes 1.31"})
    void staysUndecidedWithoutClues(String text) {
        assertThat(LanguageDetector.detect(text)).isEmpty();
    }

    @Test
    void fallsBackToTheGivenLanguageWhenUndecided() {
        assertThat(LanguageDetector.detectOr("Kubernetes 1.31", "en")).isEqualTo("en");
        assertThat(LanguageDetector.detectOr("Kubernetes 1.31", null)).isEqualTo("es");
        assertThat(LanguageDetector.detectOr("What is Docker and why should you learn it?", "es")).isEqualTo("en");
    }
}
