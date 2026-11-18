package com.nexus.backend.repository.spec;

import com.nexus.backend.entity.CommunityEvent;
import com.nexus.backend.enums.EventMode;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

/** Filtros de la agenda. Cada uno, si su valor es null, no filtra. */
public final class EventSpecifications {

    private EventSpecifications() {
    }

    /** Se ven en la web: activos y de hoy en adelante. */
    public static Specification<CommunityEvent> upcoming(LocalDate today) {
        return (root, query, cb) -> cb.and(cb.isTrue(root.get("active")),
                cb.greaterThanOrEqualTo(root.<LocalDate>get("startsOn"), today));
    }

    public static Specification<CommunityEvent> forWomen(Boolean forWomen) {
        return SpecificationUtils.equalTo("forWomen", forWomen);
    }

    public static Specification<CommunityEvent> mode(EventMode mode) {
        return SpecificationUtils.equalTo("mode", mode);
    }

    public static Specification<CommunityEvent> onDay(LocalDate day) {
        return SpecificationUtils.equalTo("startsOn", day);
    }

    /** Ciudad sin distinguir mayúsculas ni espacios de más («madrid » encuentra «Madrid»). */
    public static Specification<CommunityEvent> inCity(String city) {
        if (city == null || city.isBlank()) {
            return SpecificationUtils.all();
        }
        String normalized = city.trim().toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(cb.trim(root.get("city"))), normalized);
    }
}
