package com.nexus.backend.controller;

import com.nexus.backend.dto.request.CreateEventRequest;
import com.nexus.backend.dto.response.EventResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.enums.EventMode;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.EventService;
import com.nexus.backend.util.Pagination;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Eventos de la comunidad: verlos es público; compartirlos y borrar los propios, con sesión. */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<PageResponse<EventResponse>> getUpcoming(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_SIZE) int size,
            @RequestParam(required = false) Boolean forWomen,
            @RequestParam(required = false) EventMode mode,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(eventService.getUpcoming(forWomen, mode, page, size, principal));
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request,
                                                @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetail principal) {
        eventService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }
}
