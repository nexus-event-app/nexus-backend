package com.nexus.backend.seo;

import com.nexus.backend.entity.Guide;
import com.nexus.backend.repository.GuideRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LlmsTxtServiceTest {

    @Test
    void summarisesTheSiteAndListsEachPublishedGuideForAiAssistants() {
        GuideRepository guides = mock(GuideRepository.class);
        Guide guide = new Guide();
        guide.setSlug("sindrome-del-impostor");
        guide.setTitle("El síndrome del impostor/a");
        guide.setDescription("Qué es y cómo gestionarlo.");
        when(guides.findAllByPublishedTrueOrderByPublishedAtDesc()).thenReturn(List.of(guide));

        String text = new LlmsTxtService(guides, "https://primercommit.test/").getLlmsTxt();

        assertThat(text).startsWith("# Primer Commit\n\n> ");
        assertThat(text).contains("primer empleo tech", "bootcamp", "junior");
        assertThat(text).contains("- [Empleo](https://primercommit.test/empleo): ");
        assertThat(text).contains("- [Píldoras](https://primercommit.test/guias): ");
        assertThat(text).contains("## Píldoras\n\n- [El síndrome del impostor/a](https://primercommit.test/guias/sindrome-del-impostor): Qué es y cómo gestionarlo.\n");
        assertThat(text).contains("https://primercommit.test/sitemap.xml");
    }

    @Test
    void withoutPublishedGuidesItOnlyListsTheSections() {
        GuideRepository guides = mock(GuideRepository.class);
        when(guides.findAllByPublishedTrueOrderByPublishedAtDesc()).thenReturn(List.of());

        String text = new LlmsTxtService(guides, "https://primercommit.test").getLlmsTxt();

        assertThat(text).contains("## Secciones").doesNotContain("## Píldoras\n");
    }
}
