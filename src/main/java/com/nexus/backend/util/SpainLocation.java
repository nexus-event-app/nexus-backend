package com.nexus.backend.util;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Detecta si el texto de ubicación de una oferta es de España.
 *
 * <p>Reconoce el país, las comunidades autónomas, las capitales de provincia y otras ciudades grandes, en castellano
 * y en sus nombres oficiales (Donostia, Girona, A Coruña...). Varias ciudades tienen homónimas en Latinoamérica
 * (Valencia, Córdoba, León, Mérida...): si la ubicación menciona otro país, no cuenta como España salvo que también
 * diga «España» o «Spain». Una ubicación genérica como «Remoto», «Europe» o «Worldwide» no cuenta.
 *
 * <p>Es la única fuente de verdad: la usan la entidad al guardar y la migración que recalcula las ofertas existentes.
 */
public final class SpainLocation {

    /** Mención explícita del país: basta por sí sola. */
    private static final Pattern COUNTRY = words("espana", "spain", "espanya", "spanien", "espagne");

    private static final List<String> PLACES = List.of(
            // Comunidades autónomas y ciudades autónomas
            "andalucia", "aragon", "asturias", "cantabria", "castilla y leon", "castilla-la mancha",
            "castilla la mancha", "cataluna", "catalunya", "catalonia", "comunidad valenciana", "comunitat valenciana",
            "comunidad de madrid", "extremadura", "galicia", "islas baleares", "illes balears", "baleares",
            "islas canarias", "canarias", "la rioja", "navarra", "nafarroa", "pais vasco", "euskadi", "basque country",
            "region de murcia", "ceuta", "melilla",
            // Capitales de provincia (nombre en castellano y oficial)
            "a coruna", "la coruna", "albacete", "alicante", "alacant", "almeria", "avila", "badajoz", "barcelona",
            "bilbao", "burgos", "caceres", "cadiz", "castellon", "castello", "ciudad real", "cordoba", "cuenca",
            "donostia", "san sebastian", "girona", "gerona", "granada", "guadalajara", "huelva", "huesca", "jaen",
            "las palmas", "leon", "lleida", "lerida", "logrono", "lugo", "madrid", "malaga", "murcia", "ourense",
            "orense", "oviedo", "palencia", "palma", "palma de mallorca", "pamplona", "iruna", "pontevedra",
            "salamanca", "santa cruz de tenerife", "santander", "segovia", "sevilla", "soria", "tarragona", "teruel",
            "toledo", "valencia", "valladolid", "vitoria", "vitoria-gasteiz", "gasteiz", "zamora", "zaragoza",
            // Otras ciudades grandes o con mucho empleo tech
            "vigo", "gijon", "hospitalet", "l'hospitalet", "elche", "elx", "badalona", "terrassa", "sabadell",
            "cartagena", "jerez", "mostoles", "alcala de henares", "fuenlabrada", "leganes", "getafe", "alcorcon",
            "marbella", "santiago de compostela", "sant cugat", "las rozas", "pozuelo", "alcobendas", "tres cantos",
            "boadilla", "majadahonda", "san sebastian de los reyes", "torrejon", "mataro", "reus", "ferrol", "merida",
            "ibiza", "eivissa", "mallorca", "tenerife", "gran canaria", "menorca", "benidorm");

    private static final Pattern PLACE = words(PLACES.toArray(String[]::new));

    /** Países con ciudades homónimas de ciudades españolas, o que suelen aparecer junto a ellas. */
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

    /** Coincidencia de palabras o expresiones completas, sin distinguir mayúsculas ni acentos. */
    private static Pattern words(String... terms) {
        String alternatives = String.join("|", List.of(terms).stream().map(Pattern::quote).toList());
        return Pattern.compile("(?<![\\p{L}\\p{N}])(" + alternatives + ")(?![\\p{L}\\p{N}])");
    }
}
