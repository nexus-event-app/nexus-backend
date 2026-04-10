package com.nexus.backend.util;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Component;

@Component
public class HtmlSanitizer {

    private final PolicyFactory policy;

    public HtmlSanitizer() {
        this.policy = Sanitizers.FORMATTING
                .and(Sanitizers.LINKS)
                .and(Sanitizers.BLOCKS);
    }

    public String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return policy.sanitize(input);
    }
}