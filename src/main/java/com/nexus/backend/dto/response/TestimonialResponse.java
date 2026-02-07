package com.nexus.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialResponse {
    
    private Long id;
    private String name;
    private String role;
    private String location;
    private String content;
    private Boolean isActive;
    private Boolean isPending;
    private Boolean isHighlighted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}