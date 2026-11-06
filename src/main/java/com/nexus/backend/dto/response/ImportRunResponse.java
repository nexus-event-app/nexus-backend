package com.nexus.backend.dto.response;

import com.nexus.backend.enums.ImportStatus;

/** Resultado de lanzar una importación manual, junto con el estado actualizado de la fuente. */
public record ImportRunResponse(ImportStatus status, int fetched, int imported, String message,
                                ImportSourceResponse source) {
}
