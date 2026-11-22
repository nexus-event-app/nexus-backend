package com.nexus.backend.security.google;

public sealed interface GoogleLoginOutcome {

    record SignedIn(String code) implements GoogleLoginOutcome {
    }

    record SignupPending(String token) implements GoogleLoginOutcome {
    }

    record Rejected(String reason) implements GoogleLoginOutcome {
    }
}
