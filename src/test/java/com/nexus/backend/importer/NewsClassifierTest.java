package com.nexus.backend.importer;

import com.nexus.backend.enums.NewsCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** El tema de una noticia sale de su título y su resumen; la fuente solo desempata o rellena. */
class NewsClassifierTest {

    @Test
    void readsTheTopicFromTheTitleInSpanishAndEnglish() {
        assertThat(classify("OpenAI presenta un nuevo modelo de lenguaje", "")).isEqualTo(NewsCategory.IA);
        assertThat(classify("Hugging Face and OpenRouter join forces for open-weight LLMs", "")).isEqualTo(NewsCategory.IA);
        assertThat(classify("PostgreSQL 18 llega con mejoras en las consultas", "")).isEqualTo(NewsCategory.DATOS);
        assertThat(classify("Qué es una base de datos y para qué sirve", "")).isEqualTo(NewsCategory.DATOS);
        assertThat(classify("Una vulnerabilidad en routers permite robar contraseñas", "")).isEqualTo(NewsCategory.CIBERSEGURIDAD);
        assertThat(classify("Ubuntu 26.04 ya disponible con el kernel 6.20", "")).isEqualTo(NewsCategory.SISTEMAS);
        assertThat(classify("Cómo preparar tu primera entrevista técnica siendo junior", "")).isEqualTo(NewsCategory.CARRERA);
        assertThat(classify("Django, la alternativa a FastAPI con baterías incluidas", "")).isEqualTo(NewsCategory.PROGRAMACION);
        assertThat(classify("TypeScript 6.0: novedades para desarrolladores", "")).isEqualTo(NewsCategory.PROGRAMACION);
    }

    @Test
    void ignoresAccentsCaseAndPartsOfWords() {
        assertThat(classify("La INTELIGENCIA ARTIFICIAL llega a la oficina", "")).isEqualTo(NewsCategory.IA);
        // «ia» dentro de otra palabra (noticia, historia) no cuenta como IA; «java» no se confunde con JavaScript.
        assertThat(classify("La historia de una noticia curiosa", "", NewsCategory.GENERAL)).isEqualTo(NewsCategory.GENERAL);
        assertThat(NewsClassifier.classify("Java 25 ya está aquí", "", NewsCategory.GENERAL)).isEqualTo(NewsCategory.PROGRAMACION);
        // «Código» suelto no es programación (el Código Civil); «código fuente», sí.
        assertThat(classify("El Código Civil permite a los propietarios cortar las ramas", "")).isEqualTo(NewsCategory.GENERAL);
        assertThat(classify("Publican el código fuente del primer navegador", "")).isEqualTo(NewsCategory.PROGRAMACION);
    }

    @Test
    void espionageOnServersIsSecurityNotSystems() {
        assertThat(NewsClassifier.classify("Cereal: cómo un grupo de ciberespionaje se oculta en servidores Exchange",
                "Un análisis de la puerta trasera que usa el grupo.", NewsCategory.CIBERSEGURIDAD))
                .isEqualTo(NewsCategory.CIBERSEGURIDAD);
    }

    @Test
    void withoutAClearTopicKeepsTheSourceCategory() {
        assertThat(NewsClassifier.classify("Un día en la oficina", "Nada técnico por aquí.", NewsCategory.SISTEMAS))
                .isEqualTo(NewsCategory.SISTEMAS);
        assertThat(NewsClassifier.classify("Un día en la oficina", "", null)).isEqualTo(NewsCategory.GENERAL);
        // Una sola mención en el resumen no basta para cambiar de tema.
        assertThat(NewsClassifier.classify("Novedades de la semana", "También hablamos de Python.", NewsCategory.GENERAL))
                .isEqualTo(NewsCategory.GENERAL);
    }

    @Test
    void aSpecialisedSourceWinsWhenTheContentIsBalanced() {
        // Seguridad e IA a partes iguales: en una web de seguridad, se queda en Ciberseguridad.
        String title = "Cómo usan la IA los ciberdelincuentes";
        assertThat(NewsClassifier.classify(title, "", NewsCategory.CIBERSEGURIDAD)).isEqualTo(NewsCategory.CIBERSEGURIDAD);
        assertThat(NewsClassifier.classify(title, "", NewsCategory.IA)).isEqualTo(NewsCategory.IA);
    }

    private static NewsCategory classify(String title, String summary) {
        return classify(title, summary, NewsCategory.GENERAL);
    }

    private static NewsCategory classify(String title, String summary, NewsCategory source) {
        return NewsClassifier.classify(title, summary, source);
    }
}
