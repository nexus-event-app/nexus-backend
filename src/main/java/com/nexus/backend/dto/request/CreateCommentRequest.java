package com.nexus.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    
    @NotNull(message = "El ID de la discusión es obligatorio")
    private Long discussionId;
    
    @NotBlank(message = "El contenido es obligatorio")
    @Size(min = 3, max = 5000, message = "El comentario debe tener entre 3 y 5000 caracteres")
    private String content;
}