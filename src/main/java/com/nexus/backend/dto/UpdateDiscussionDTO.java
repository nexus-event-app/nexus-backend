package com.nexus.backend.dto;

import com.nexus.backend.enums.DiscussionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDiscussionDTO {
    
    @NotBlank(message = "El título no puede estar vacío")
    @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres")
    private String title;
    
    @NotBlank(message = "El contenido no puede estar vacío")
    @Size(min = 10, message = "El contenido debe tener al menos 10 caracteres")
    private String content;
    
    @NotNull(message = "La categoría es obligatoria")
    private DiscussionCategory category;
}