package com.nexus.backend.repository.spec;

import com.nexus.backend.entity.JobOffer;
import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.WorkMode;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class JobSpecifications {

    private JobSpecifications() {
    }

    /** Ofertas visibles al público: activas y no caducadas. */
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
        return SpecificationUtils.<JobOffer>equalTo("level", level)
                .and(SpecificationUtils.<JobOffer>equalTo("workMode", workMode))
                .and(SpecificationUtils.<JobOffer>equalTo("contractType", contractType))
                .and(SpecificationUtils.<JobOffer>textContains(text, "title", "company", "description"));
    }
}
