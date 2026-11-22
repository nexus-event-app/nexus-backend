package com.nexus.backend.controller;

import com.nexus.backend.dto.request.DeleteAccountRequest;
import com.nexus.backend.dto.request.UpdateProfileRequest;
import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.dto.response.ProfileResponse;
import com.nexus.backend.dto.response.TestimonialResponse;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.AccountDeletionService;
import com.nexus.backend.service.ProfileService;
import com.nexus.backend.util.Pagination;
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

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final AccountDeletionService accountDeletionService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(profileService.getOwnProfile(principal));
    }

    @GetMapping("/discussions")
    public ResponseEntity<PageResponse<DiscussionResponse>> getDiscussions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_SIZE) int size,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(profileService.getOwnDiscussions(principal, page, size));
    }

    @GetMapping("/testimonials")
    public ResponseEntity<List<TestimonialResponse>> getTestimonials(@AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(profileService.getOwnTestimonials(principal));
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

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@Valid @RequestBody(required = false) DeleteAccountRequest request,
                                              @AuthenticationPrincipal UserDetail principal) {
        accountDeletionService.deleteOwnAccount(request == null ? new DeleteAccountRequest() : request, principal);
        return ResponseEntity.noContent().build();
    }
}
