package com.nexus.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNewsRequest {
    
    private String title;
    private String content;
    private String sourceUrl;
    private String imageUrl;
    private String author;
    private Boolean isActive;
}