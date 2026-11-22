package com.nexus.backend.enums;

import java.time.Duration;

public enum UserTokenType {

    EMAIL_VERIFICATION(Duration.ofHours(48)),
    PASSWORD_RESET(Duration.ofHours(1));

    private final Duration validity;

    UserTokenType(Duration validity) {
        this.validity = validity;
    }

    public Duration getValidity() {
        return validity;
    }
}
