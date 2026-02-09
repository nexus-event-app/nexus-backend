package com.nexus.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.nexus.backend.enums.NewsCategory;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewsRequest {
    
    @NotBlank(message = "El título es obligatorio")
    private String title;
    
    private String content;
    
    private String sourceUrl;
    
    private String imageUrl;
    
    private String author;
    
    private NewsCategory category = NewsCategory.GENERAL;

}