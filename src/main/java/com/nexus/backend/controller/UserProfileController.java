package com.nexus.backend.controller;

import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.dto.response.ProfileResponse;
import com.nexus.backend.dto.response.TestimonialResponse;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.ProfileService;
import com.nexus.backend.util.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Perfiles públicos de la comunidad: datos del perfil, discusiones y testimonios publicados. */
@RestController
@RequestMapping("/api/users/{id}")
@RequiredArgsConstructor
public class UserProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getPublicProfile(id));
    }

    @GetMapping("/discussions")
    public ResponseEntity<PageResponse<DiscussionResponse>> getDiscussions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_SIZE) int size,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(profileService.getDiscussions(id, page, size, principal));
    }

    @GetMapping("/testimonials")
    public ResponseEntity<List<TestimonialResponse>> getTestimonials(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getTestimonials(id));
    }
}
