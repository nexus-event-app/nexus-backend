package com.nexus.backend.repository.spec;

import com.nexus.backend.entity.News;
import com.nexus.backend.enums.NewsCategory;
import org.springframework.data.jpa.domain.Specification;

public final class NewsSpecifications {

    private NewsSpecifications() {
    }

    public static Specification<News> isActive() {
        return (root, query, cb) -> cb.isTrue(root.<Boolean>get("isActive"));
    }

    public static Specification<News> filter(NewsCategory category, String text) {
        return SpecificationUtils.<News>equalTo("category", category)
                .and(SpecificationUtils.<News>textContains(text, "title", "content"));
    }
}
