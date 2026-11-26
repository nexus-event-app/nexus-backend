package com.nexus.backend.seo;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nexus.backend.entity.Guide;
import com.nexus.backend.i18n.Languages;
import com.nexus.backend.repository.GuideRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class LlmsTxtService {

    private static final String CACHE_KEY = "llms";
    private static final List<Section> SECTIONS = List.of(
            new Section("Píldoras", "/guias",
                    "guías cortas y prácticas para dar los primeros pasos en tech: después del bootcamp, entrevistas técnicas, Git y GitHub, meetups y síndrome del impostor"),
            new Section("Empleo", "/empleo",
                    "ofertas de prácticas y empleo junior en tecnología en España, filtradas por nivel, modalidad, contrato y ciudad"),
            new Section("Eventos", "/eventos",
                    "calendario de meetups, charlas, hackathons y talleres tech, presenciales y en línea"),
            new Section("Foro", "/foro",
                    "preguntas y respuestas entre personas que empiezan en programación y tecnología"),
            new Section("Rutas de aprendizaje", "/rutas",
                    "itinerarios paso a paso para frontend, backend, QA, datos e inteligencia artificial"),
            new Section("Noticias", "/noticias", "actualidad del sector tecnológico explicada para perfiles junior"),
            new Section("Glosario tech", "/glosario", "términos de programación explicados de forma sencilla"),
            new Section("Testimonios", "/testimonios", "experiencias reales de personas de la comunidad"),
            new Section("Sobre el proyecto", "/sobre-el-proyecto", "qué es Primer Commit y por qué existe"));

    private final GuideRepository guideRepository;
    private final String publicUrl;

    private final Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(1)
            .build();

    public LlmsTxtService(GuideRepository guideRepository,
                          @Value("${app.public-url:http://localhost:5173}") String publicUrl) {
        this.guideRepository = guideRepository;
        this.publicUrl = publicUrl.trim().replaceAll("/+$", "");
    }

    public String getLlmsTxt() {
        return cache.get(CACHE_KEY, key -> build());
    }

    private String build() {
        StringBuilder text = new StringBuilder(4096);
        text.append("# Primer Commit\n\n");
        text.append("> Comunidad gratuita para personas que empiezan en tecnología en España: guías prácticas (Píldoras), ")
                .append("ofertas de empleo junior y prácticas, eventos tech, foro de dudas y rutas de aprendizaje ")
                .append("para conseguir el primer empleo tech después de un bootcamp, un ciclo o aprendiendo por tu cuenta.\n\n");
        text.append("La web está en castellano, catalán e inglés. Las páginas en catalán empiezan por ")
                .append(publicUrl).append("/ca y las de inglés por ").append(publicUrl).append("/en.\n\n");

        text.append("## Secciones\n\n");
        for (Section section : SECTIONS) {
            text.append("- [").append(section.title()).append("](").append(url(section.path())).append("): ")
                    .append(section.summary()).append('\n');
        }

        List<Guide> guides = guideRepository.findAllByPublishedTrueOrderByPublishedAtDesc();
        if (!guides.isEmpty()) {
            text.append("\n## Píldoras\n\n");
            for (Guide guide : guides) {
                text.append("- [").append(guide.getTitle()).append("](").append(url("/guias/" + guide.getSlug()))
                        .append("): ").append(guide.getDescription()).append('\n');
            }
        }

        text.append("\n## Optional\n\n");
        text.append("- [Mapa del sitio](").append(publicUrl).append("/sitemap.xml): todas las páginas públicas, con sus versiones en cada idioma\n");
        return text.toString();
    }

    private String url(String path) {
        return publicUrl + Languages.localizePath(Languages.DEFAULT, path);
    }

    private record Section(String title, String path, String summary) {
    }
}
