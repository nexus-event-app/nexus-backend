package com.nexus.backend.dto.request;

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
public class CreateDiscussionRequest {
    
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar 200 caracteres")
    private String title;
    
    @NotBlank(message = "El contenido es obligatorio")
    private String content;
    
    @NotNull(message = "La categoría es obligatoria")
    private DiscussionCategory category;
}