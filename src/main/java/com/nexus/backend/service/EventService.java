package com.nexus.backend.service;

import com.nexus.backend.dto.request.EventRequest;
import com.nexus.backend.dto.response.AdminEventResponse;
import com.nexus.backend.dto.response.EventDayResponse;
import com.nexus.backend.dto.response.EventInterestResponse;
import com.nexus.backend.dto.response.EventResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.enums.EventMode;
import com.nexus.backend.security.UserDetail;

import java.time.LocalDate;
import java.util.List;

public interface EventService {

    record Filter(Boolean forWomen, EventMode mode, String city, LocalDate day) {
    }

    PageResponse<EventResponse> getUpcoming(Filter filter, int page, int size, UserDetail viewer);

    List<EventDayResponse> getCalendar(LocalDate from, LocalDate to);

    List<String> getCities();

    EventResponse create(EventRequest request, UserDetail actor);

    EventInterestResponse toggleInterest(Long id, UserDetail actor);

    AdminEventResponse update(Long id, EventRequest request);

    void delete(Long id, UserDetail actor);

    PageResponse<AdminEventResponse> getAllForModeration(int page, int size);

    boolean isVisible(Long id);

    void hide(Long id);
}
