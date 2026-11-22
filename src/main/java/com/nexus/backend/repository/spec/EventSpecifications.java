package com.nexus.backend.repository.spec;

import com.nexus.backend.entity.CommunityEvent;
import com.nexus.backend.enums.EventMode;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

public final class EventSpecifications {

    private EventSpecifications() {
    }

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

    public static Specification<CommunityEvent> inCity(String city) {
        if (city == null || city.isBlank()) {
            return SpecificationUtils.all();
        }
        String normalized = city.trim().toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(cb.trim(root.get("city"))), normalized);
    }
}
