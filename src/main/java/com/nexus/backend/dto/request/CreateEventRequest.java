package com.nexus.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {
    
    @NotBlank(message = "El título es obligatorio")
    private String title;
    
    private String description;
    
    @NotNull(message = "La fecha del evento es obligatoria")
    private LocalDateTime eventDate;
    
    @NotBlank(message = "La ubicación es obligatoria")
    private String location;
    
    @NotBlank(message = "El tipo de evento es obligatorio")
    private String eventType;
    
    @NotBlank(message = "El nivel es obligatorio")
    private String level;
    
    private String tags;
    
    private String sourceUrl;
    
    private String organizerName;
}