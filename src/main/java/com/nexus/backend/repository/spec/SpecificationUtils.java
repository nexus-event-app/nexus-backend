package com.nexus.backend.repository.spec;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class SpecificationUtils {

    private static final int MAX_SEARCH_LENGTH = 100;

    private SpecificationUtils() {
    }

    public static <T> Specification<T> all() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static <T> Specification<T> equalTo(String attribute, Object value) {
        if (value == null) {
            return all();
        }
        return (root, query, cb) -> cb.equal(root.get(attribute), value);
    }

    /** Búsqueda de texto (sin distinguir mayúsculas) en cualquiera de los atributos indicados. */
    public static <T> Specification<T> textContains(String text, String... attributes) {
        if (text == null || text.isBlank()) {
            return all();
        }
        String trimmed = text.trim();
        if (trimmed.length() > MAX_SEARCH_LENGTH) {
            trimmed = trimmed.substring(0, MAX_SEARCH_LENGTH);
        }
        String pattern = "%" + escapeLike(trimmed.toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> {
            Predicate[] predicates = new Predicate[attributes.length];
            for (int i = 0; i < attributes.length; i++) {
                predicates[i] = cb.like(cb.lower(root.<String>get(attributes[i])), pattern, '\\');
            }
            return cb.or(predicates);
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
