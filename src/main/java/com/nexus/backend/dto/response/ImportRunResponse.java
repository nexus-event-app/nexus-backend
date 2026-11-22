package com.nexus.backend.dto.response;

import com.nexus.backend.enums.ImportStatus;

public record ImportRunResponse(ImportStatus status, int fetched, int imported, String message,
                                ImportSourceResponse source) {
}
