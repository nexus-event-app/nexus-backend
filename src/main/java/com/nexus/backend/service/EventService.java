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

    /** Filtros de la agenda; cualquiera puede ser null (no filtra). */
    record Filter(Boolean forWomen, EventMode mode, String city, LocalDate day) {
    }

    /** Próximos eventos visibles, del más cercano al más lejano. viewer puede ser null (sin sesión). */
    PageResponse<EventResponse> getUpcoming(Filter filter, int page, int size, UserDetail viewer);

    /** Días con eventos entre dos fechas (como mucho dos meses), para el calendario. */
    List<EventDayResponse> getCalendar(LocalDate from, LocalDate to);

    /** Ciudades con eventos presenciales próximos, ordenadas, para el filtro. */
    List<String> getCities();

    EventResponse create(EventRequest request, UserDetail actor);

    /** «Me apunto»: lo marca o lo quita (solo en eventos visibles) y devuelve el número actualizado. */
    EventInterestResponse toggleInterest(Long id, UserDetail actor);

    /** Panel: corregir un evento (cualquier fecha, por si hubo un error al compartirlo). */
    AdminEventResponse update(Long id, EventRequest request);

    /** Borrar: quien lo compartió o una cuenta de administración (p. ej., si está repetido). */
    void delete(Long id, UserDetail actor);

    PageResponse<AdminEventResponse> getAllForModeration(int page, int size);

    /** Lo que usa la moderación de reportes. */
    boolean isVisible(Long id);

    void hide(Long id);
}
