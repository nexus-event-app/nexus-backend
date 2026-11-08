package com.nexus.backend.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Detecta si el texto de ubicación de una oferta es de España. Solo se reconocen el país y ciudades sin
 * ambigüedad habitual; una ubicación como «Remoto» o «Worldwide» no cuenta como España.
 */
public final class SpainLocation {

    private static final Pattern SPAIN = Pattern.compile(
            "(?<![a-z0-9])(espana|spain|madrid|barcelona|bilbao|sevilla|malaga|zaragoza|valencia, espana|"
                    + "palma de mallorca|las palmas|santa cruz de tenerife|a coruna|vigo|granada)(?![a-z0-9])");

    private SpainLocation() {
    }

    public static boolean matches(String location) {
        if (location == null || location.isBlank()) {
            return false;
        }
        String normalized = Normalizer.normalize(location, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        return SPAIN.matcher(normalized).find();
    }
}
