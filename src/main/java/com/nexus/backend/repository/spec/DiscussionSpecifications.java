package com.nexus.backend.repository.spec;

import com.nexus.backend.entity.Discussion;
import com.nexus.backend.enums.DiscussionCategory;
import org.springframework.data.jpa.domain.Specification;

public final class DiscussionSpecifications {

    private DiscussionSpecifications() {
    }

    public static Specification<Discussion> isActive() {
        return (root, query, cb) -> cb.isTrue(root.<Boolean>get("isActive"));
    }

    public static Specification<Discussion> hasAuthor(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("author").get("id"), userId);
    }

    public static Specification<Discussion> filter(DiscussionCategory category, String text) {
        return SpecificationUtils.<Discussion>equalTo("category", category)
                .and(SpecificationUtils.<Discussion>textContains(text, "title", "content"));
    }
}
