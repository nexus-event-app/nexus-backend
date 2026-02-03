package com.nexus.backend.mapper;

import com.nexus.backend.dto.request.CreateEventRequest;
import com.nexus.backend.dto.request.UpdateEventRequest;
import com.nexus.backend.dto.response.EventResponse;
import com.nexus.backend.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    
    public Event toEntity(CreateEventRequest request) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setLocation(request.getLocation());
        event.setEventType(request.getEventType());
        event.setLevel(request.getLevel());
        event.setTags(request.getTags());
        event.setSourceUrl(request.getSourceUrl());
        event.setOrganizerName(request.getOrganizerName());
        event.setIsActive(true);
        return event;
    }
    
    public void updateEntity(Event event, UpdateEventRequest request) {
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getEventType() != null) {
            event.setEventType(request.getEventType());
        }
        if (request.getLevel() != null) {
            event.setLevel(request.getLevel());
        }
        if (request.getTags() != null) {
            event.setTags(request.getTags());
        }
        if (request.getSourceUrl() != null) {
            event.setSourceUrl(request.getSourceUrl());
        }
        if (request.getOrganizerName() != null) {
            event.setOrganizerName(request.getOrganizerName());
        }
        if (request.getIsActive() != null) {
            event.setIsActive(request.getIsActive());
        }
    }
    
    public EventResponse toResponse(Event event) {
        return new EventResponse(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getEventDate(),
            event.getLocation(),
            event.getEventType(),
            event.getLevel(),
            event.getTags(),
            event.getSourceUrl(),
            event.getOrganizerName(),
            event.getIsActive(),
            event.getCreatedAt(),
            event.getUpdatedAt()
        );
    }
}