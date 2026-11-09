package com.nexus.backend.importer;

import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.enums.ImportSourceType;
import com.nexus.backend.enums.NewsCategory;

import java.util.List;

/**
 * Fuentes de importación recomendadas. Se crean al arrancar con una base de datos vacía y, desde el panel, se pueden
 * añadir las que falten («Añadir fuentes recomendadas»). Cada llamada devuelve objetos nuevos, sin guardar.
 */
public final class DefaultImportSources {

    /**
     * Genbeta mezcla tecnología con economía, política y curiosidades. Solo se importa lo que habla de desarrollo,
     * datos, IA aplicada, ciberseguridad o empleo tech. No se usan términos sueltos como «IA», «ChatGPT», «Google» o
     * «Microsoft» porque dejan pasar noticias de empresas que no son de tecnología.
     */
    public static final String GENBETA_KEYWORDS = "programación, programador, desarrollador, desarrolladora, "
            + "software, código, código abierto, open source, GitHub, Python, JavaScript, TypeScript, Java, Rust, "
            + "Linux, Docker, DevOps, API, framework, algoritmo, base de datos, bases de datos, SQL, PostgreSQL, "
            + "MongoDB, ciberseguridad, vulnerabilidad, modelo de lenguaje, LLM, machine learning, "
            + "aprendizaje automático, agentes de IA, IA generativa, nube, cloud, developer, ingeniero de software";

    /** Temas que no encajan en una comunidad junior aunque mencionen tecnología. */
    public static final String GENBETA_EXCLUDED = "contenido para adultos, porno, pornografía, sexual, apuestas, "
            + "casino, préstamo, préstamos, hipoteca, criptomoneda, criptomonedas, bitcoin, NFT, aerolínea, "
            + "Palantir, Bezos, Elon Musk, Trump, elecciones, partido político, sindicatos, atraco, robo";

    private DefaultImportSources() {
    }

    public static List<ImportSource> create() {
        return List.of(
                news("freeCodeCamp en español", "https://www.freecodecamp.org/espanol/news/rss/", NewsCategory.RECURSOS, true),
                filtered(news("Genbeta", "https://www.genbeta.com/feedburner.xml", NewsCategory.GENERAL, true),
                        GENBETA_KEYWORDS, GENBETA_EXCLUDED),
                inactive(withoutSourceImages(news("DEV Community · #beginners", "https://dev.to/feed/tag/beginners",
                        NewsCategory.DESARROLLO, false))),
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

    /** Sus portadas son tarjetas de texto generadas automáticamente: mejor las fotos propias de la web. */
    private static ImportSource withoutSourceImages(ImportSource source) {
        source.setUseSourceImages(false);
        return source;
    }

    private static ImportSource filtered(ImportSource source, String keywords, String excluded) {
        source.setKeywords(keywords);
        source.setExcludeKeywords(excluded);
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
