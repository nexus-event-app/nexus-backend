package com.nexus.backend.moderation;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class ContentFilter {

    public enum Level {
        CLEAN, HOLD, BLOCK
    }

    public record Verdict(Level level, List<String> words) {

        static final Verdict CLEAN = new Verdict(Level.CLEAN, List.of());

        public boolean isClean() {
            return level == Level.CLEAN;
        }
    }

    private static final List<String> SEVERE = List.of(
            "maricon(a|as|es)?", "maricas?", "bolleras?", "tortilleras?", "travelos?", "sudacas?", "panchit[oa]s?",
            "negrat[ao]s?", "subnormal(es)?", "mongol[oa]s?", "mongolic[oa]s?", "feminazis?", "marico(ns)?",
            "hij[oa]s? de put[ao]s?", "hij[oa]s? de perras?", "fill[as]? de puta", "fills de puta",
            "retrasad[oa]s? mental(es)?", "te voy a matar", "os voy a matar", "ojala te mueras", "suicidate",
            "matate", "muerete", "mor te",
            "nigg(er|ers|a|as|ah)", "faggots?", "fags?", "dykes?", "trann(y|ies)", "retards?", "retarded", "kikes?",
            "spics?", "chinks?", "wetbacks?", "cunts?", "bitch(es)?", "whores?", "sluts?", "kill yourself", "kys");

    private static final List<String> MILD = List.of(
            "jod(er|ido|ida|idos|idas|ete|eos|e|o|an|en)", "mierdas?", "conyos?", "cony", "cabron(a|as|es)?",
            "cojones", "cojonud[oa]s?", "gilipollas", "gilipolles", "imbecil(es|s)?", "idiotas?", "idiotes",
            "estupid[oa]s?", "capull[oa]s?", "pollas?", "hostias?", "put[ao]s?", "mamon(a|as|es)?", "pendej[oa]s?",
            "carajo", "ching(ar|ada|ado|ue|a|o)", "vergas?", "culer[oa]s?", "zorras?", "me cago", "cagada",
            "merd(a|es)", "collons", "fotre", "cabro(ns)?",
            "fuck(s|er|ers|ing|in|ed)?", "motherfuck(er|ers|ing)?", "shit(s|ty)?", "bullshit", "assholes?",
            "bastards?", "dicks?", "crap", "wtf");

    private static final List<List<Pattern>> SEVERE_PATTERNS = compile(SEVERE);
    private static final List<List<Pattern>> MILD_PATTERNS = compile(MILD);
    private static final Pattern REPEATED = Pattern.compile("(\\p{L})\\1{2,}");
    private static final Pattern SEPARATOR = Pattern.compile("[^a-z0-9]+");

    private ContentFilter() {
    }

    public static Verdict check(String... texts) {
        List<String> tokens = new ArrayList<>();
        for (String text : texts) {
            if (text != null) {
                tokens.addAll(tokens(text));
            }
        }
        Set<String> severe = find(tokens, SEVERE_PATTERNS);
        if (!severe.isEmpty()) {
            return new Verdict(Level.BLOCK, List.copyOf(severe));
        }
        Set<String> mild = find(tokens, MILD_PATTERNS);
        return mild.isEmpty() ? Verdict.CLEAN : new Verdict(Level.HOLD, List.copyOf(mild));
    }

    static List<String> tokens(String text) {
        String lower = text.toLowerCase(Locale.ROOT).replace("ñ", "ny");
        String plain = Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String collapsed = REPEATED.matcher(plain).replaceAll("$1");
        List<String> tokens = new ArrayList<>();
        StringBuilder spelled = new StringBuilder();
        for (String raw : SEPARATOR.split(unLeet(collapsed))) {
            if (raw.isEmpty()) {
                continue;
            }
            if (raw.length() == 1) {
                spelled.append(raw);
                continue;
            }
            flushSpelled(spelled, tokens);
            tokens.add(raw);
        }
        flushSpelled(spelled, tokens);
        return tokens;
    }

    private static void flushSpelled(StringBuilder spelled, List<String> tokens) {
        if (!spelled.isEmpty()) {
            tokens.add(spelled.toString());
            spelled.setLength(0);
        }
    }

    private static String unLeet(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            boolean insideWord = (i > 0 && Character.isLetter(text.charAt(i - 1)))
                    || (i + 1 < text.length() && Character.isLetter(text.charAt(i + 1)));
            out.append(insideWord ? switch (current) {
                case '0' -> 'o';
                case '1' -> 'i';
                case '3' -> 'e';
                case '4', '@' -> 'a';
                case '5', '$' -> 's';
                case '7' -> 't';
                default -> current;
            } : current);
        }
        return out.toString();
    }

    private static Set<String> find(List<String> tokens, List<List<Pattern>> patterns) {
        Set<String> found = new LinkedHashSet<>();
        for (int start = 0; start < tokens.size(); start++) {
            for (List<Pattern> phrase : patterns) {
                if (start + phrase.size() <= tokens.size() && matches(tokens, start, phrase)) {
                    found.add(String.join(" ", tokens.subList(start, start + phrase.size())));
                }
            }
        }
        return found;
    }

    private static boolean matches(List<String> tokens, int start, List<Pattern> phrase) {
        for (int offset = 0; offset < phrase.size(); offset++) {
            if (!phrase.get(offset).matcher(tokens.get(start + offset)).matches()) {
                return false;
            }
        }
        return true;
    }

    private static List<List<Pattern>> compile(List<String> entries) {
        return entries.stream()
                .map(entry -> List.of(entry.split(" ")).stream().map(Pattern::compile).toList())
                .toList();
    }
}
