package com.nexus.backend.config;

import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.enums.ImportSourceType;
import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.importer.ImportProperties;
import com.nexus.backend.repository.ImportSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Crea un conjunto inicial de fuentes la primera vez que arranca la aplicación (si no hay ninguna).
 * Después se gestionan desde el panel de administración.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImportSourceInitializer implements CommandLineRunner {

    /**
     * Genbeta mezcla tecnología con economía, política y curiosidades. Solo se importa lo que habla de desarrollo,
     * datos, IA aplicada, ciberseguridad o empleo tech. No se usan términos sueltos como «IA», «ChatGPT»,
     * «Google» o «Microsoft» porque dejan pasar noticias de empresas que no son de tecnología.
     */
    private static final String GENBETA_KEYWORDS = "programación, programador, desarrollador, desarrolladora, "
            + "software, código, código abierto, open source, GitHub, Python, JavaScript, TypeScript, Java, Rust, "
            + "Linux, Docker, DevOps, API, framework, algoritmo, base de datos, bases de datos, SQL, PostgreSQL, "
            + "MongoDB, ciberseguridad, vulnerabilidad, modelo de lenguaje, LLM, machine learning, "
            + "aprendizaje automático, agentes de IA, IA generativa, nube, cloud, developer, ingeniero de software";


    private final ImportSourceRepository sourceRepository;
    private final ImportProperties properties;

    @Override
    public void run(String... args) {
        if (!properties.seedDefaults() || sourceRepository.count() > 0) {
            return;
        }
        List<ImportSource> defaults = List.of(
                news("freeCodeCamp en español", "https://www.freecodecamp.org/espanol/news/rss/", NewsCategory.RECURSOS, true),
                withKeywords(news("Genbeta", "https://www.genbeta.com/feedburner.xml", NewsCategory.GENERAL, true),
                        GENBETA_KEYWORDS),
                inactive(news("DEV Community · #beginners", "https://dev.to/feed/tag/beginners", NewsCategory.DESARROLLO, false)),
                jobs("Get on Board · Programación", ImportSourceType.GETONBRD_JOBS,
                        "https://www.getonbrd.com/api/v0/categories/programming/jobs?per_page=100", false),
                jobs("Get on Board · DevOps y QA", ImportSourceType.GETONBRD_JOBS,
                        "https://www.getonbrd.com/api/v0/categories/sysadmin-devops-qa/jobs?per_page=100", false),
                jobs("Get on Board · Datos", ImportSourceType.GETONBRD_JOBS,
                        "https://www.getonbrd.com/api/v0/categories/data-science-analytics/jobs?per_page=100", false),
                jobs("Remotive", ImportSourceType.REMOTIVE_JOBS,
                        "https://remotive.com/api/remote-jobs?category=software-dev&search=junior", false),
                jobs("Jobicy · Europa", ImportSourceType.JOBICY_JOBS,
                        "https://jobicy.com/api/v2/remote-jobs?count=100&geo=europe&industry=dev", false),
                jobs("Arbeitnow", ImportSourceType.ARBEITNOW_JOBS, "https://www.arbeitnow.com/api/job-board-api", true));
        sourceRepository.saveAll(defaults);
        log.info("Creadas {} fuentes de importación por defecto", defaults.size());
    }

    /** Las fuentes en inglés se crean sin publicación automática para que un admin revise lo importado. */
    private static ImportSource news(String name, String url, NewsCategory category, boolean autoPublish) {
        ImportSource source = base(name, ImportSourceType.RSS_NEWS, url);
        source.setNewsCategory(category);
        source.setAutoPublish(autoPublish);
        source.setIntervalMinutes(180);
        return source;
    }

    /** Fuente creada pero apagada: un admin puede activarla desde el panel si le sirve. */
    private static ImportSource inactive(ImportSource source) {
        source.setActive(false);
        return source;
    }

    private static ImportSource withKeywords(ImportSource source, String keywords) {
        source.setKeywords(keywords);
        return source;
    }

    private static ImportSource jobs(String name, ImportSourceType type, String url, boolean remoteOnly) {
        ImportSource source = base(name, type, url);
        source.setRemoteOnly(remoteOnly);
        source.setIntervalMinutes(type.getMinIntervalMinutes());
        return source;
    }

    private static ImportSource base(String name, ImportSourceType type, String url) {
        ImportSource source = new ImportSource();
        source.setName(name);
        source.setType(type);
        source.setUrl(url);
        source.setActive(true);
        source.setAutoPublish(true);
        source.setRemoteOnly(false);
        return source;
    }
}
