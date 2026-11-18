package com.nexus.backend.util;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Reconoce la ciudad dentro de un texto que mezcla dirección y ciudad («42 Málaga Fundación Telefónica, Av. de Sor
 * Teresa Prat, 15» → «Málaga»), para que el filtro de la agenda tenga solo ciudades. Capitales de provincia y las
 * ciudades más grandes de España, sin acentos ni mayúsculas al comparar.
 */
public final class SpanishCities {

    /** Las de varias palabras van antes, para que «Las Palmas de Gran Canaria» no se quede en «Palma». */
    private static final List<String> CITIES = List.of(
            "Las Palmas de Gran Canaria", "Santa Cruz de Tenerife", "San Sebastián", "Palma de Mallorca",
            "Vitoria-Gasteiz", "A Coruña", "La Coruña", "Ciudad Real", "Alcalá de Henares", "Jerez de la Frontera",
            "L'Hospitalet de Llobregat", "Sant Cugat del Vallès",
            "Madrid", "Barcelona", "Valencia", "València", "Sevilla", "Zaragoza", "Málaga", "Murcia", "Palma",
            "Bilbao", "Alicante", "Córdoba", "Valladolid", "Vigo", "Gijón", "Granada", "Elche", "Oviedo", "Badalona",
            "Cartagena", "Terrassa", "Sabadell", "Móstoles", "Santander", "Burgos", "Almería", "Castellón", "Albacete",
            "Getafe", "Alcorcón", "Logroño", "Badajoz", "Salamanca", "Huelva", "Marbella", "Lleida", "Lérida",
            "Tarragona", "León", "Cádiz", "Jaén", "Ourense", "Girona", "Gerona", "Lugo", "Cáceres", "Guadalajara",
            "Toledo", "Pontevedra", "Palencia", "Zamora", "Ávila", "Segovia", "Cuenca", "Soria", "Teruel", "Huesca",
            "Pamplona", "Iruña", "Santiago de Compostela", "Donostia", "Vitoria", "Mataró", "Reus", "Leganés",
            "Fuenlabrada", "Torrejón de Ardoz", "Las Rozas", "Pozuelo de Alarcón", "Dos Hermanas", "Benidorm",
            "Ponferrada", "Ferrol", "Avilés", "Mérida", "Algeciras", "Granollers", "Manresa", "Castelldefels");

    private static final List<Pattern> PATTERNS = CITIES.stream()
            .map(city -> Pattern.compile("(?<![\\p{L}\\p{N}])" + Pattern.quote(normalize(city)) + "(?![\\p{L}\\p{N}])"))
            .toList();

    private SpanishCities() {
    }

    /** La primera ciudad conocida que aparece en el texto, escrita como en la lista. */
    public static Optional<String> find(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        String normalized = normalize(text);
        for (int i = 0; i < CITIES.size(); i++) {
            if (PATTERNS.get(i).matcher(normalized).find()) {
                return Optional.of(CITIES.get(i));
            }
        }
        return Optional.empty();
    }

    /** ¿Parece una dirección y no solo una ciudad? Lleva números, comas o es demasiado larga. */
    public static boolean looksLikeAddress(String text) {
        return text != null && (text.matches(".*\\d.*") || text.contains(",") || text.contains("·") || text.length() > 40);
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
