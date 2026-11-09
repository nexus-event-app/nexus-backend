package com.nexus.backend.controller;

import com.nexus.backend.dto.request.UpdateProfileRequest;
import com.nexus.backend.dto.response.ProfileResponse;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Perfil de la persona con sesión iniciada: consultarlo, editarlo y cambiar o quitar la foto. */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(profileService.getOwnProfile(principal));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(profileService.updateProfile(request, principal));
    }

    @PostMapping("/avatar")
    public ResponseEntity<ProfileResponse> updateAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(profileService.updateAvatar(file, principal));
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<ProfileResponse> removeAvatar(@AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(profileService.removeAvatar(principal));
    }
}
