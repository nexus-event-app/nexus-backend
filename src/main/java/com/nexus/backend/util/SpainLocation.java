package com.nexus.backend.util;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SpainLocation {

    private static final Pattern COUNTRY = words("espana", "spain", "espanya", "spanien", "espagne");

    private static final List<String> PLACES = List.of(
            "andalucia", "aragon", "asturias", "cantabria", "castilla y leon", "castilla-la mancha",
            "castilla la mancha", "cataluna", "catalunya", "catalonia", "comunidad valenciana", "comunitat valenciana",
            "comunidad de madrid", "extremadura", "galicia", "islas baleares", "illes balears", "baleares",
            "islas canarias", "canarias", "la rioja", "navarra", "nafarroa", "pais vasco", "euskadi", "basque country",
            "region de murcia", "ceuta", "melilla",
            "a coruna", "la coruna", "albacete", "alicante", "alacant", "almeria", "avila", "badajoz", "barcelona",
            "bilbao", "burgos", "caceres", "cadiz", "castellon", "castello", "ciudad real", "cordoba", "cuenca",
            "donostia", "san sebastian", "girona", "gerona", "granada", "guadalajara", "huelva", "huesca", "jaen",
            "las palmas", "leon", "lleida", "lerida", "logrono", "lugo", "madrid", "malaga", "murcia", "ourense",
            "orense", "oviedo", "palencia", "palma", "palma de mallorca", "pamplona", "iruna", "pontevedra",
            "salamanca", "santa cruz de tenerife", "santander", "segovia", "sevilla", "soria", "tarragona", "teruel",
            "toledo", "valencia", "valladolid", "vitoria", "vitoria-gasteiz", "gasteiz", "zamora", "zaragoza",
            "vigo", "gijon", "hospitalet", "l'hospitalet", "elche", "elx", "badalona", "terrassa", "sabadell",
            "cartagena", "jerez", "mostoles", "alcala de henares", "fuenlabrada", "leganes", "getafe", "alcorcon",
            "marbella", "santiago de compostela", "sant cugat", "las rozas", "pozuelo", "alcobendas", "tres cantos",
            "boadilla", "majadahonda", "san sebastian de los reyes", "torrejon", "mataro", "reus", "ferrol", "merida",
            "ibiza", "eivissa", "mallorca", "tenerife", "gran canaria", "menorca", "benidorm");

    private static final Pattern PLACE = words(PLACES.toArray(String[]::new));

    private static final Pattern OTHER_COUNTRY = words(
            "venezuela", "argentina", "mexico", "colombia", "chile", "peru", "nicaragua", "filipinas", "philippines",
            "ecuador", "bolivia", "uruguay", "paraguay", "panama", "costa rica", "cuba", "honduras", "guatemala",
            "el salvador", "puerto rico", "republica dominicana", "dominican republic", "estados unidos", "usa",
            "united states", "brasil", "brazil", "portugal", "francia", "france", "italia", "italy");

    private SpainLocation() {
    }

    public static boolean matches(String location) {
        if (location == null || location.isBlank()) {
            return false;
        }
        String text = normalize(location);
        if (COUNTRY.matcher(text).find()) {
            return true;
        }
        return PLACE.matcher(text).find() && !OTHER_COUNTRY.matcher(text).find();
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('’', '\'')
                .toLowerCase(Locale.ROOT);
    }

    private static Pattern words(String... terms) {
        String alternatives = String.join("|", List.of(terms).stream().map(Pattern::quote).toList());
        return Pattern.compile("(?<![\\p{L}\\p{N}])(" + alternatives + ")(?![\\p{L}\\p{N}])");
    }
}
