package com.nexus.backend.repository.spec;

import com.nexus.backend.entity.JobOffer;
import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.WorkMode;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Locale;

public final class JobSpecifications {

    private JobSpecifications() {
    }

    public static Specification<JobOffer> isPublished() {
        return (root, query, cb) -> {
            LocalDateTime now = LocalDateTime.now();
            return cb.and(
                    cb.isTrue(root.<Boolean>get("isActive")),
                    cb.or(
                            cb.isNull(root.<LocalDateTime>get("expiresAt")),
                            cb.greaterThan(root.<LocalDateTime>get("expiresAt"), now)));
        };
    }

    public static Specification<JobOffer> filter(JobLevel level, WorkMode workMode, ContractType contractType, String text) {
        return filter(level, workMode, contractType, null, text);
    }

    public static Specification<JobOffer> filter(JobLevel level, WorkMode workMode, ContractType contractType, String city,
                                                 String text) {
        return SpecificationUtils.<JobOffer>equalTo("level", level)
                .and(SpecificationUtils.<JobOffer>equalTo("workMode", workMode))
                .and(SpecificationUtils.<JobOffer>equalTo("contractType", contractType))
                .and(inCity(city))
                .and(SpecificationUtils.<JobOffer>textContains(text, "title", "company", "description"));
    }

    public static Specification<JobOffer> inCity(String city) {
        if (city == null || city.isBlank()) {
            return (root, query, cb) -> null;
        }
        String normalized = city.trim().toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(root.get("city")), normalized);
    }
}
