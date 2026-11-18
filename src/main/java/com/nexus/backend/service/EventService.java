package com.nexus.backend.service;

import com.nexus.backend.dto.request.CreateEventRequest;
import com.nexus.backend.dto.response.AdminEventResponse;
import com.nexus.backend.dto.response.EventResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.enums.EventMode;
import com.nexus.backend.security.UserDetail;

public interface EventService {

    /** Próximos eventos visibles; forWomen y mode opcionales. viewer puede ser null (sin sesión). */
    PageResponse<EventResponse> getUpcoming(Boolean forWomen, EventMode mode, int page, int size, UserDetail viewer);

    EventResponse create(CreateEventRequest request, UserDetail actor);

    /** Borrar: quien lo compartió o una cuenta de administración (p. ej., si está repetido). */
    void delete(Long id, UserDetail actor);

    PageResponse<AdminEventResponse> getAllForModeration(int page, int size);

    /** Lo que usa la moderación de reportes. */
    boolean isVisible(Long id);

    void hide(Long id);
}
