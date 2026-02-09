package com.nexus.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.nexus.backend.enums.NewsCategory;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponse {
    
    private Long id;
    private String title;
    private String content;
    private String sourceUrl;
    private String imageUrl;
    private String author;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private NewsCategory category;

}