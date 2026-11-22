package com.nexus.backend.dto.response;

import java.time.LocalDateTime;

public record SavedJobResponse(JobResponse job, boolean available, LocalDateTime savedAt) {
}
