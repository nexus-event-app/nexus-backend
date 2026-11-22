package com.nexus.backend.controller;

import com.nexus.backend.dto.request.EventRequest;
import com.nexus.backend.dto.response.EventDayResponse;
import com.nexus.backend.dto.response.EventInterestResponse;
import com.nexus.backend.dto.response.EventResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.enums.EventMode;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.EventService;
import com.nexus.backend.util.Pagination;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

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
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(eventService.getUpcoming(new EventService.Filter(forWomen, mode, city, day), page, size,
                principal));
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<EventDayResponse>> getCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(eventService.getCalendar(from, to));
    }

    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        return ResponseEntity.ok(eventService.getCities());
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request,
                                                @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request, principal));
    }

    @PostMapping("/{id}/interest")
    public ResponseEntity<EventInterestResponse> toggleInterest(@PathVariable Long id,
                                                                @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(eventService.toggleInterest(id, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetail principal) {
        eventService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }
}
