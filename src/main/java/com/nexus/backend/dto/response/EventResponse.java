package com.nexus.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String location;
    private String eventType;
    private String level;
    private String tags;
    private String sourceUrl;
    private String organizerName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}