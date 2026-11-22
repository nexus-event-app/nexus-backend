package com.nexus.backend.controller;

import com.nexus.backend.dto.request.GoogleSignupRequest;
import com.nexus.backend.dto.request.GoogleTokenRequest;
import com.nexus.backend.dto.response.AuthProvidersResponse;
import com.nexus.backend.dto.response.AuthResponse;
import com.nexus.backend.dto.response.GoogleSignupResponse;
import com.nexus.backend.security.google.GoogleAccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    private final GoogleAccountService googleAccounts;
    private final boolean googleEnabled;

    public GoogleAuthController(GoogleAccountService googleAccounts,
                                @Value("${app.google.client-id:}") String googleClientId) {
        this.googleAccounts = googleAccounts;
        this.googleEnabled = googleClientId != null && !googleClientId.isBlank();
    }

    @GetMapping("/providers")
    public AuthProvidersResponse providers() {
        return new AuthProvidersResponse(googleEnabled);
    }

    @PostMapping("/google/exchange")
    public AuthResponse exchange(@Valid @RequestBody GoogleTokenRequest request) {
        return googleAccounts.exchange(request.getToken());
    }

    @PostMapping("/google/pending")
    public GoogleSignupResponse pending(@Valid @RequestBody GoogleTokenRequest request) {
        return googleAccounts.pendingSignup(request.getToken());
    }

    @PostMapping("/google/complete")
    public ResponseEntity<AuthResponse> complete(@Valid @RequestBody GoogleSignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(googleAccounts.completeSignup(request.getToken()));
    }
}
