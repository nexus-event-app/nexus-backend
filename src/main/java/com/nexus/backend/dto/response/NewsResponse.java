package com.nexus.backend.dto.response;

import com.nexus.backend.enums.NewsCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponse {

    private Long id;
    private String slug;
    private String title;
    private String content;
    private String sourceUrl;
    private String imageUrl;
    private String author;
    private NewsCategory category;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
