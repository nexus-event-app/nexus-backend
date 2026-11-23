package com.nexus.backend.dto.response;

import com.nexus.backend.enums.EventMode;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventResponse(Long id, String title, String description, String url, String host, LocalDate date,
                            LocalTime time, EventMode mode, String city, String address, boolean forWomen,
                            boolean mine, long interestedCount, boolean interested,
                            boolean held) {
}
