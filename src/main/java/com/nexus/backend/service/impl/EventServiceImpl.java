package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateEventRequest;
import com.nexus.backend.dto.request.UpdateEventRequest;
import com.nexus.backend.dto.response.EventResponse;
import com.nexus.backend.entity.Event;
import com.nexus.backend.mapper.EventMapper;
import com.nexus.backend.repository.EventRepository;
import com.nexus.backend.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    
    @Override
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        Event event = eventMapper.toEntity(request);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toResponse(savedEvent);
    }
    
    @Override
    public List<EventResponse> getAllActiveEvents() {
        return eventRepository.findByIsActiveTrue()
            .stream()
            .map(eventMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + id));
        return eventMapper.toResponse(event);
    }
    
    @Override
    @Transactional
    public EventResponse updateEvent(Long id, UpdateEventRequest request) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + id));
        
        eventMapper.updateEntity(event, request);
        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toResponse(updatedEvent);
    }
    
    @Override
    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado con ID: " + id));
        
        event.setIsActive(false);
        eventRepository.save(event);
    }
    
    @Override
    public List<EventResponse> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findByEventDateAfterAndIsActiveTrue(now)
            .stream()
            .map(eventMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<EventResponse> getPastEvents() {
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findByEventDateBeforeAndIsActiveTrue(now)
            .stream()
            .map(eventMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<EventResponse> getEventsByLocation(String location) {
        return eventRepository.findByLocation(location)
            .stream()
            .map(eventMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<EventResponse> getEventsByType(String eventType) {
        return eventRepository.findByEventType(eventType)
            .stream()
            .map(eventMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
public List<EventResponse> getLatestEvents() {
    return eventRepository.findTop3ByIsActiveTrueOrderByCreatedAtDesc()
        .stream()
        .map(eventMapper::toResponse)
        .collect(Collectors.toList());
}
}