package com.nexus.backend.importer;

import com.nexus.backend.enums.JobLevel;

import java.text.Normalizer;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Decide si una oferta importada encaja en Nexus: nivel prácticas o junior y del sector tech.
 * Trabaja con el título, las etiquetas y las pistas de nivel del origen (en español, inglés,
 * alemán y francés), sin acentos y en minúsculas.
 */
public final class JobClassifier {

    private static final Pattern SENIOR = Pattern.compile(
            "\\b(senior|sr|lead|leiter\\w*|principal|staff|head|director\\w*|chief|architect|arquitect\\w*|"
                    + "vp|expert\\w*|experto|experta|teamleitung|manager)\\b");
    private static final Pattern SENIOR_HINT = Pattern.compile(
            "\\b(experienced|berufserfahren|mid|mid-senior|senior|executive|professional|professionnel|experimente|"
                    + "manager|geschaftsleitung|teamleitung)\\b");
    private static final Pattern INTERN = Pattern.compile(
            "\\b(intern|interns|internship|praktikum|praktikant\\w*|pflichtpraktikum|hochschulpraktikant\\w*|"
                    + "practica|practicas|practicante\\w*|becari[oa]s?|trainee|werkstudent\\w*|working student|apprentice\\w*|"
                    + "ausbildung|stage|stagiaire|student college|convention de stage)\\b");
    private static final Pattern JUNIOR = Pattern.compile(
            "\\b(junior|jr|entry-level|entry level|graduate|berufseinstieg\\w*|berufseinsteiger\\w*|einsteiger\\w*|"
                    + "debutant\\w*|sin experiencia|no experience required|recien titulad\\w*)\\b");
    /** En las pistas del origen, "entry" a secas sí indica nivel de entrada. */
    private static final Pattern ENTRY_HINT = Pattern.compile("\\bentry\\b");
    private static final Pattern TECH = Pattern.compile(
            "\\b(developer\\w*|desarrollador\\w*|programador\\w*|programmer|entwickler\\w*|softwareentwickl\\w*|"
                    + "software|frontend|front-end|backend|back-end|fullstack|full-stack|full stack|web|webentwickl\\w*|"
                    + "devops|cloud|data|datos|machine learning|ml|ai|qa|tester|testing|sysadmin|"
                    + "it|informatik\\w*|informatiker\\w*|fachinformatiker\\w*|wirtschaftsinformatik\\w*|"
                    + "anwendungsentwicklung|cyber\\w*|security|ciberseguridad|java|javascript|typescript|python|php|"
                    + "ruby|golang|rust|kotlin|swift|react|angular|vue|node|sql|android|ios|mobile|ux|ui|"
                    + "engineer|engineering|ingenier[oa] de software|soporte tecnico|helpdesk)\\b");

    /** Puestos que contienen palabras tech pero no lo son. */
    private static final Pattern NON_TECH = Pattern.compile(
            "\\b(business developer\\w*|business development|sales|ventas|vertrieb\\w*|account manager|recruiter)\\b");

    private JobClassifier() {
    }

    /** Nivel de la oferta, o vacío si no es de prácticas ni junior. */
    public static Optional<JobLevel> level(String title, Collection<String> levelHints) {
        String normalizedTitle = normalize(title);
        if (SENIOR.matcher(normalizedTitle).find()) {
            return Optional.empty();
        }
        Optional<JobLevel> fromTitle = match(normalizedTitle);
        if (fromTitle.isPresent()) {
            return fromTitle;
        }
        String hints = normalize(String.join(" | ", levelHints));
        if (SENIOR_HINT.matcher(hints).find()) {
            return Optional.empty();
        }
        return match(hints).or(() -> ENTRY_HINT.matcher(hints).find() ? Optional.of(JobLevel.JUNIOR) : Optional.empty());
    }

    /** Si el título o las etiquetas indican que la oferta es del sector tech. */
    public static boolean isTech(String title, Collection<String> tags) {
        String normalizedTitle = normalize(title);
        if (NON_TECH.matcher(normalizedTitle).find()) {
            return false;
        }
        return TECH.matcher(normalizedTitle + " | " + normalize(String.join(" | ", tags))).find();
    }

    private static Optional<JobLevel> match(String text) {
        if (INTERN.matcher(text).find()) {
            return Optional.of(JobLevel.INTERN);
        }
        if (JUNIOR.matcher(text).find()) {
            return Optional.of(JobLevel.JUNIOR);
        }
        return Optional.empty();
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return withoutAccents.replace('ß', 's').toLowerCase(Locale.ROOT);
    }
}
