package com.nexus.backend.dto.response;

import com.nexus.backend.enums.EventMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminEventResponse(Long id, String title, String description, String url, String host, LocalDate date,
                                 LocalTime time, EventMode mode, String city, String address, boolean forWomen,
                                 boolean active,
                                 boolean past, String authorName, LocalDateTime createdAt) {
}
