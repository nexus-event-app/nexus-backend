package com.nexus.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.nexus.backend.enums.NewsCategory;


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
    private NewsCategory category;

}