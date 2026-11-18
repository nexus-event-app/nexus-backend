package com.nexus.backend.controller;

import com.nexus.backend.dto.request.EventRequest;
import com.nexus.backend.dto.response.AdminEventResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.service.EventService;
import com.nexus.backend.util.Pagination;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Panel: todos los eventos (también ocultos y pasados) para revisar y corregir; se borran con DELETE /api/events/{id}. */
@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<PageResponse<AdminEventResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_SIZE) int size) {
        return ResponseEntity.ok(eventService.getAllForModeration(page, size));
    }

    /** Corregir un evento (un error de fecha, de ciudad, el enlace...). */
    @PutMapping("/{id}")
    public ResponseEntity<AdminEventResponse> update(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.update(id, request));
    }
}
