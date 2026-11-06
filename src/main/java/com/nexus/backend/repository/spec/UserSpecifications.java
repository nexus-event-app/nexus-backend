package com.nexus.backend.repository.spec;

import com.nexus.backend.entity.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> matches(String text) {
        return SpecificationUtils.<User>textContains(text, "name", "email");
    }
}
