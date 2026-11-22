package com.nexus.backend.importer;

import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.enums.ImportSourceType;
import com.nexus.backend.enums.NewsCategory;

import java.util.List;

public final class DefaultImportSources {

    public static final String GENBETA_KEYWORDS = "programación, programador, desarrollador, desarrolladora, "
            + "software, código fuente, código abierto, open source, GitHub, Python, JavaScript, TypeScript, Java, Rust, "
            + "Linux, Docker, DevOps, API, framework, algoritmo, base de datos, bases de datos, SQL, PostgreSQL, "
            + "MongoDB, ciberseguridad, vulnerabilidad, modelo de lenguaje, LLM, machine learning, "
            + "aprendizaje automático, agentes de IA, IA generativa, nube, cloud, developer, ingeniero de software";

    public static final String GENBETA_EXCLUDED = "contenido para adultos, porno, pornografía, sexual, apuestas, "
            + "casino, préstamo, préstamos, hipoteca, criptomoneda, criptomonedas, bitcoin, NFT, aerolínea, "
            + "Palantir, Bezos, Elon Musk, Trump, elecciones, partido político, sindicatos, atraco, robo";

    static final String PARADIGMA_URL = "https://www.paradigmadigital.com/feed/";
    static final String WELIVESECURITY_URL = "https://www.welivesecurity.com/es/rss/feed/";
    static final String HIPERTEXTUAL_URL = "https://hipertextual.com/feed";

    public static final String SHOPPING_EXCLUDED = "chollo, chollos, oferta, ofertas, descuento, rebaja, rebajas, "
            + "Black Friday, Prime Day, precio mínimo, cupón";

    private DefaultImportSources() {
    }

    static final String ADZUNA_SPAIN_URL = "https://api.adzuna.com/v1/api/jobs/es/search?category=it-jobs"
            + "&what_or=junior%20becario%20becaria%20practicas%20trainee%20intern%20internship&max_days_old=30";

    static final String XATAKA_IA_URL = "https://www.xataka.com/tag/inteligencia-artificial/rss2.xml";
    static final String MUYLINUX_URL = "https://www.muylinux.com/feed/";
    static final String MUYCOMPUTERPRO_URL = "https://www.muycomputerpro.com/feed";
    static final String ELPAIS_TECNOLOGIA_URL =
            "https://feeds.elpais.com/mrss-s/pages/ep/site/elpais.com/section/tecnologia/portada";
    static final String GOOGLE_BLOG_ES_URL = "https://blog.google/intl/es-es/rss/";

    public static List<ImportSource> create() {
        return List.of(
                news("freeCodeCamp en español", "https://www.freecodecamp.org/espanol/news/rss/", NewsCategory.PROGRAMACION, true),
                filtered(news("Genbeta", "https://www.genbeta.com/feedburner.xml", NewsCategory.GENERAL, true),
                        GENBETA_KEYWORDS, GENBETA_EXCLUDED),
                news("Paradigma Digital", PARADIGMA_URL, NewsCategory.PROGRAMACION, true),
                news("WeLiveSecurity en español", WELIVESECURITY_URL, NewsCategory.CIBERSEGURIDAD, true),
                filtered(news("Hipertextual", HIPERTEXTUAL_URL, NewsCategory.GENERAL, true),
                        GENBETA_KEYWORDS, GENBETA_EXCLUDED + ", " + SHOPPING_EXCLUDED),
                filtered(news("Xataka · Inteligencia artificial", XATAKA_IA_URL, NewsCategory.IA, false),
                        null, GENBETA_EXCLUDED + ", " + SHOPPING_EXCLUDED),
                news("MuyLinux", MUYLINUX_URL, NewsCategory.SISTEMAS, true),
                filtered(news("MuyComputerPro", MUYCOMPUTERPRO_URL, NewsCategory.GENERAL, false),
                        GENBETA_KEYWORDS, GENBETA_EXCLUDED + ", " + SHOPPING_EXCLUDED),
                filtered(news("El País · Tecnología", ELPAIS_TECNOLOGIA_URL, NewsCategory.GENERAL, true),
                        GENBETA_KEYWORDS, GENBETA_EXCLUDED),
                news("Blog de Google en España", GOOGLE_BLOG_ES_URL, NewsCategory.GENERAL, true),
                english(inactive(withoutSourceImages(news("DEV Community · #beginners", "https://dev.to/feed/tag/beginners",
                        NewsCategory.PROGRAMACION, false)))),
                english(news("GitHub Blog", "https://github.blog/feed/", NewsCategory.PROGRAMACION, false)),
                english(news("Stack Overflow Blog", "https://stackoverflow.blog/feed/", NewsCategory.PROGRAMACION, false)),
                english(news("freeCodeCamp (English)", "https://www.freecodecamp.org/news/rss/", NewsCategory.PROGRAMACION, false)),
                english(news("Hugging Face Blog", "https://huggingface.co/blog/feed.xml", NewsCategory.IA, false)),
                english(news("PostgreSQL News", "https://www.postgresql.org/news.rss", NewsCategory.DATOS, false)),
                english(news("Python Insider", "https://blog.python.org/feeds/posts/default?alt=rss", NewsCategory.PROGRAMACION, false)),
                english(news("JetBrains Blog", "https://blog.jetbrains.com/feed/", NewsCategory.PROGRAMACION, false)),
                english(news("CSS-Tricks", "https://css-tricks.com/feed/", NewsCategory.PROGRAMACION, false)),
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
                jobs("Arbeitnow", ImportSourceType.ARBEITNOW_JOBS, "https://www.arbeitnow.com/api/job-board-api", true),
                jobs("Adzuna · España", ImportSourceType.ADZUNA_JOBS, ADZUNA_SPAIN_URL, false));
    }

    private static ImportSource news(String name, String url, NewsCategory category, boolean autoPublish) {
        ImportSource source = base(name, ImportSourceType.RSS_NEWS, url);
        source.setNewsCategory(category);
        source.setAutoPublish(autoPublish);
        source.setIntervalMinutes(180);
        return source;
    }

    private static ImportSource english(ImportSource source) {
        source.setLanguage("en");
        return source;
    }

    private static ImportSource inactive(ImportSource source) {
        source.setActive(false);
        return source;
    }

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
