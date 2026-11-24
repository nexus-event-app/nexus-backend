package com.nexus.backend.util;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class SpanishCities {

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

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("València", "Valencia"), Map.entry("La Coruña", "A Coruña"), Map.entry("Lérida", "Lleida"),
            Map.entry("Gerona", "Girona"), Map.entry("Iruña", "Pamplona"), Map.entry("Donostia", "San Sebastián"),
            Map.entry("Vitoria", "Vitoria-Gasteiz"), Map.entry("Palma de Mallorca", "Palma"));

    private SpanishCities() {
    }

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

    public static Optional<String> canonical(String text) {
        return find(text).map(city -> ALIASES.getOrDefault(city, city));
    }

    public static boolean looksLikeAddress(String text) {
        return text != null && (text.matches(".*\\d.*") || text.contains(",") || text.contains("·") || text.length() > 40);
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
