package com.nexus.backend.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class LanguageDetector {

    private static final Set<String> SPANISH = Set.of(
            "el", "la", "los", "las", "un", "una", "unos", "unas", "de", "del", "al", "y", "o", "que", "en", "con",
            "para", "por", "como", "pero", "mas", "es", "son", "esta", "este", "estos", "estas", "ese", "esa", "su",
            "sus", "se", "lo", "le", "les", "ya", "muy", "tambien", "sin", "sobre", "entre", "cuando", "donde", "porque",
            "hay", "ser", "hacer", "puede", "pueden", "tu", "nuevo", "nueva", "nuevos", "nuevas", "ahora", "todo",
            "todos", "asi", "desde", "hasta", "segun", "aunque", "cual", "cada", "otro", "otra", "mejor", "despues");

    private static final Set<String> ENGLISH = Set.of(
            "the", "an", "of", "and", "or", "to", "in", "on", "for", "with", "by", "from", "at", "is", "are",
            "was", "were", "be", "been", "this", "that", "these", "those", "it", "its", "you", "your", "we", "our",
            "they", "their", "how", "what", "why", "when", "which", "who", "will", "can", "should", "would", "about",
            "into", "new", "not", "but", "if", "than", "more", "just", "has", "have", "do", "does", "get", "using",
            "use", "my", "i", "all", "out", "up", "so", "there", "here", "after", "before", "first");

    private static final Set<String> OTHER = Set.of(
            "der", "die", "das", "und", "ist", "nicht", "mit", "ein", "eine", "ich", "sie", "auf", "fur", "auch", "wie",
            "wir", "zu", "dem", "von", "sich", "oder", "aber", "wenn", "mehr", "nach", "uber", "bei", "sind", "wird",
            "des", "est", "une", "pour", "dans", "qui", "pas", "sur", "avec", "ce", "ces", "sont", "mais", "nous",
            "vous", "il", "elle", "du", "aux", "ou", "plus", "tout", "comme", "cette", "faire", "leur", "votre", "vos",
            "notre", "comment", "peut", "etre", "aussi", "chez", "tres",
            "nao", "em", "da", "dos", "voce", "ao", "sao", "isso", "pelo", "pela", "seu",
            "sua", "foi", "tem", "um", "uma", "quem", "muito", "ela", "ele", "entao", "voces",
            "della", "che", "sono", "per", "non", "gli", "nel", "alla", "delle", "questo", "anche", "piu", "essere",
            "het", "een", "van", "niet", "dat", "zijn", "voor", "met", "op", "ook", "wat", "je", "ik", "maar", "naar",
            "bij", "worden",
            "og", "et", "er", "ikke", "det", "jeg", "til", "af", "pa", "som", "med", "har", "kan", "den", "att",
            "och", "inte", "ar", "jag", "om", "hvad", "hvordan", "eller", "nar", "blive", "bliver", "efter", "sig",
            "ogsa", "hvis", "noget", "kun");

    private static final int MIN_MARGIN = 2;

    private static final Pattern NON_LETTERS = Pattern.compile("[^\\p{L}]+");
    private static final Pattern MARKS = Pattern.compile("\\p{M}+");

    private LanguageDetector() {
    }

    public static Optional<String> detect(String text) {
        Scores scores = score(text);
        if (scores.isOther()) {
            return Optional.empty();
        }
        if (scores.spanish() - scores.english() >= MIN_MARGIN) {
            return Optional.of(ContentLanguage.SPANISH);
        }
        if (scores.english() - scores.spanish() >= MIN_MARGIN) {
            return Optional.of(ContentLanguage.ENGLISH);
        }
        return Optional.empty();
    }

    public static boolean isOtherLanguage(String text) {
        return score(text).isOther();
    }

    private static Scores score(String text) {
        if (text == null || text.isBlank()) {
            return new Scores(0, 0, 0);
        }
        int spanish = countSpanishMarks(text);
        int english = 0;
        int other = 0;
        String plain = MARKS.matcher(Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD))
                .replaceAll("");
        for (String word : NON_LETTERS.split(plain)) {
            if (SPANISH.contains(word)) {
                spanish++;
            }
            if (ENGLISH.contains(word)) {
                english++;
            }
            if (OTHER.contains(word)) {
                other++;
            }
        }
        return new Scores(spanish, english, other);
    }

    private record Scores(int spanish, int english, int other) {
        boolean isOther() {
            return other - Math.max(spanish, english) >= MIN_MARGIN;
        }
    }

    public static String detectOr(String text, String fallback) {
        return detect(text).orElse(ContentLanguage.orDefault(fallback));
    }

    private static int countSpanishMarks(String text) {
        int marks = 0;
        for (char character : text.toCharArray()) {
            if (character == 'ñ' || character == 'Ñ' || character == '¿' || character == '¡') {
                marks += 2;
            }
        }
        return marks;
    }
}
