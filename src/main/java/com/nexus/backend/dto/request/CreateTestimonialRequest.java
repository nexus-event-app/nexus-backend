package com.nexus.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestimonialRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    private String role;
    
    private String location;
    
    @NotBlank(message = "El testimonio es obligatorio")
    @Size(min = 50, max = 300, message = "El testimonio debe tener entre 50 y 300 caracteres")
    private String content;
}