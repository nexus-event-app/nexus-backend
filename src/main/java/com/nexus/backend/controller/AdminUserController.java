package com.nexus.backend.controller;

import com.nexus.backend.dto.request.UpdateProfileRequest;
import com.nexus.backend.dto.request.UpdateUserRoleRequest;
import com.nexus.backend.dto.request.UpdateUserStatusRequest;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.dto.response.ProfileResponse;
import com.nexus.backend.dto.response.UserResponse;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.ProfileService;
import com.nexus.backend.service.UserService;
import com.nexus.backend.util.Pagination;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cuentas desde el panel: buscarlas, activarlas o desactivarlas, dar o quitar el permiso de administración y
 * revisar o corregir su perfil. Solo para administradores (SecurityConfig protege /api/admin/**).
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> searchUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_SIZE) int size,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(userService.searchUsers(q, page, size));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(userService.updateStatus(id, request.getEnabled(), principal));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(userService.updateRole(id, request.getRole(), principal));
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfileForAdmin(id));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Long id, @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfileAsAdmin(id, request));
    }

    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<ProfileResponse> removeAvatar(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.removeAvatarAsAdmin(id));
    }
}
