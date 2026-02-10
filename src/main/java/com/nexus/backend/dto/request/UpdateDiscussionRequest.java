package com.nexus.backend.dto.request;

import com.nexus.backend.enums.DiscussionCategory;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDiscussionRequest {
    
    @Size(max = 200, message = "El título no puede superar 200 caracteres")
    private String title;
    
    private String content;
    
    private DiscussionCategory category;
    
    private Boolean isActive;
}