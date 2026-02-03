package com.nexus.backend.controller;

import com.nexus.backend.dto.request.CreateEventRequest;
import com.nexus.backend.dto.request.UpdateEventRequest;
import com.nexus.backend.dto.response.EventResponse;
import com.nexus.backend.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {
    
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllActiveEvents() {
        List<EventResponse> events = eventService.getAllActiveEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request) {
        EventResponse response = eventService.updateEvent(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponse>> getUpcomingEvents() {
        List<EventResponse> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/past")
    public ResponseEntity<List<EventResponse>> getPastEvents() {
        List<EventResponse> events = eventService.getPastEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<EventResponse>> getEventsByLocation(@PathVariable String location) {
        List<EventResponse> events = eventService.getEventsByLocation(location);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<EventResponse>> getEventsByType(@PathVariable String type) {
        List<EventResponse> events = eventService.getEventsByType(type);
        return ResponseEntity.ok(events);
    }
}