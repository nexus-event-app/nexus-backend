package com.nexus.backend.importer;

import com.nexus.backend.enums.NewsCategory;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NewsClassifier {

    private static final int TITLE_WEIGHT = 3;
    private static final int SUMMARY_WEIGHT = 1;
    private static final int SOURCE_BONUS = 3;
    private static final int MIN_SCORE = 3;

    private static final Map<NewsCategory, Pattern> RULES = new LinkedHashMap<>();

    static {
        RULES.put(NewsCategory.CIBERSEGURIDAD, words(
                "ciberseguridad", "cybersecurity", "ciberataques?", "cyberattacks?", "ciberdelincuen\\w*", "ciberespionaje",
                "espionaje", "espionage", "backdoors?", "puertas? traseras?", "amenazas?", "threats?", "apt\\d*",
                "comprometid[oa]s?", "atacantes?", "attackers?", "ataques?", "intrusi(on|ones)",
                "vulnerabilidad(es)?", "vulnerabilit\\w*", "malware", "ransomware", "phishing", "smishing",
                "hackers?", "hackeos?", "brechas? de (datos|seguridad)", "data breach\\w*", "contrasenas?",
                "passwords?", "cve-\\d+", "exploits?", "troyanos?", "spyware", "estafas?", "fraudes?",
                "seguridad informatica", "autenticacion en dos pasos", "2fa", "parches? de seguridad",
                "security (update|patch|flaw)\\w*"));
        RULES.put(NewsCategory.DATOS, words(
                "bases? de datos", "databases?", "sql", "postgres(ql)?", "mysql", "mariadb", "mongodb", "redis",
                "sqlite", "nosql", "data engineer\\w*", "ingenier\\w* de datos", "analisis de datos",
                "analistas? de datos", "data analy\\w*", "data science", "ciencia de datos", "cientific\\w* de datos",
                "big data", "etl", "data warehouse", "pandas", "power bi", "tableau", "dataset\\w*",
                "conjuntos? de datos"));
        RULES.put(NewsCategory.IA, words(
                "inteligencia artificial", "ia", "ai", "artificial intelligence", "machine learning",
                "aprendizaje automatico", "deep learning", "aprendizaje profundo", "redes neuronales",
                "neural networks?", "llms?", "modelos? de lenguaje", "language models?", "chatgpt", "openai",
                "gpt-?\\d\\w*", "gemini", "claude", "anthropic", "copilot", "mistral", "llama", "hugging face",
                "agentes? de ia", "ai agents?", "agentic", "ia generativa", "generative ai", "chatbots?", "prompts?"));
        RULES.put(NewsCategory.SISTEMAS, words(
                "linux", "ubuntu", "debian", "fedora", "arch linux", "kernel", "distros?", "distribucion(es)? linux",
                "docker", "kubernetes", "k8s", "contenedores", "containers?", "devops", "cloud", "nube",
                "aws", "azure", "google cloud", "servidor(es)?", "servers?", "terraform", "ansible", "ci/cd",
                "sysadmin", "red hat", "openshift", "virtualizacion", "virtualization", "systemd",
                "centros? de datos", "data cent(er|re)s?", "open source", "codigo abierto", "gnome", "kde"));
        RULES.put(NewsCategory.CARRERA, words(
                "empleos?", "ofertas? de (empleo|trabajo)", "entrevistas? (de trabajo|tecnicas?)", "curriculum",
                "cv", "linkedin", "networking", "juniors?", "primer empleo", "buscar trabajo",
                "busqueda de empleo", "salarios?", "sueldos?", "bootcamps?", "formacion", "carrera (profesional|en tecnologia|tech|en tech)",
                "reclutador\\w*", "recruiters?", "hiring", "jobs?", "careers?", "job interviews?", "resumes?",
                "portfolio", "recien graduad\\w*", "becari\\w*", "practicas", "despidos?", "layoffs?",
                "mercado laboral", "teletrabajo", "remote work"));
        RULES.put(NewsCategory.PROGRAMACION, words(
                "programacion", "programador\\w*", "programming", "programmers?", "desarrollador\\w*", "developers?",
                "desarrollo (web|de software|movil|de aplicaciones)", "software", "codigo fuente", "source code", "coding",
                "javascript", "typescript", "python", "java", "kotlin", "rust", "golang", "php", "ruby", "swift",
                "react", "angular", "vue", "svelte", "next\\.?js", "node\\.?js", "deno", "django", "flask", "fastapi",
                "spring( boot)?", "frameworks?", "librerias?", "libraries", "apis?", "frontend", "front-end",
                "backend", "back-end", "full ?stack", "html", "css", "webassembly", "github", "git", "compilador\\w*",
                "compilers?", "visual studio", "vs code", "android studio", "testing", "tests? unitarios?"));
    }

    private NewsClassifier() {
    }

    public static NewsCategory classify(String title, String summary, NewsCategory sourceCategory) {
        NewsCategory fallback = sourceCategory == null ? NewsCategory.GENERAL : sourceCategory;
        String normalizedTitle = normalize(title);
        String normalizedSummary = normalize(summary);

        NewsCategory best = null;
        int bestScore = 0;
        for (Map.Entry<NewsCategory, Pattern> rule : RULES.entrySet()) {
            int score = count(rule.getValue(), normalizedTitle) * TITLE_WEIGHT
                    + count(rule.getValue(), normalizedSummary) * SUMMARY_WEIGHT;
            if (score > 0 && rule.getKey() == fallback) {
                score += SOURCE_BONUS;
            }
            if (score > bestScore) {
                best = rule.getKey();
                bestScore = score;
            }
        }
        return bestScore >= MIN_SCORE ? best : fallback;
    }

    private static int count(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        int found = 0;
        while (matcher.find()) {
            found++;
        }
        return found;
    }

    private static Pattern words(String... terms) {
        return Pattern.compile("(?<![\\p{L}\\p{N}])(" + String.join("|", terms) + ")(?![\\p{L}\\p{N}])");
    }

    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
