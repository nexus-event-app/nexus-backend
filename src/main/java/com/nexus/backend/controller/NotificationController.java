package com.nexus.backend.controller;

import com.nexus.backend.dto.response.NotificationResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.NotificationService;
import com.nexus.backend.util.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Avisos de la persona con sesión iniciada. */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_SIZE) int size,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(notificationService.getNotifications(page, size, principal));
    }

    /** Lo consulta la campanita de la cabecera cada poco: solo devuelve el número. */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> countUnread(@AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(principal)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, @AuthenticationPrincipal UserDetail principal) {
        notificationService.markRead(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal UserDetail principal) {
        notificationService.markAllRead(principal);
        return ResponseEntity.noContent().build();
    }
}
