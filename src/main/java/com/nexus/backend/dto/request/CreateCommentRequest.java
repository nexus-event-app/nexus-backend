package com.nexus.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String content;
}