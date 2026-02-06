package com.nexus.backend.service;

import com.nexus.backend.dto.request.CreateEventRequest;
import com.nexus.backend.dto.request.UpdateEventRequest;
import com.nexus.backend.dto.response.EventResponse;
import java.util.List;

public interface EventService {
    
    EventResponse createEvent(CreateEventRequest request);
    
    List<EventResponse> getAllActiveEvents();
    
    EventResponse getEventById(Long id);
    
    EventResponse updateEvent(Long id, UpdateEventRequest request);
    
    void deleteEvent(Long id);
    
    List<EventResponse> getUpcomingEvents();
    
    List<EventResponse> getPastEvents();
    
    List<EventResponse> getEventsByLocation(String location);
    
    List<EventResponse> getEventsByType(String eventType);

    List<EventResponse> getLatestEvents();

}