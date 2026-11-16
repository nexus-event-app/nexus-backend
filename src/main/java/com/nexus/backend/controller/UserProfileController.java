package com.nexus.backend.controller;

import com.nexus.backend.dto.response.ProfileResponse;
import com.nexus.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Perfiles públicos de la comunidad: solo lo que la persona ha puesto en su perfil. Su actividad (discusiones y
 * testimonios) no se muestra a nadie más: está en /api/profile, solo para su dueño.
 */
@RestController
@RequestMapping("/api/users/{id}")
@RequiredArgsConstructor
public class UserProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getPublicProfile(id));
    }
}
