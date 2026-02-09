package com.nexus.backend.mapper;

import com.nexus.backend.dto.request.CreateNewsRequest;
import com.nexus.backend.dto.request.UpdateNewsRequest;
import com.nexus.backend.dto.response.NewsResponse;
import com.nexus.backend.entity.News;
import org.springframework.stereotype.Component;

@Component
public class NewsMapper {
    
    public News toEntity(CreateNewsRequest request) {
        News news = new News();
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setSourceUrl(request.getSourceUrl());
        news.setImageUrl(request.getImageUrl());
        news.setAuthor(request.getAuthor());
        news.setCategory(request.getCategory());
        news.setIsActive(true);
        return news;
    }
    
    public void updateEntity(News news, UpdateNewsRequest request) {
        if (request.getTitle() != null) {
            news.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            news.setContent(request.getContent());
        }
        if (request.getSourceUrl() != null) {
            news.setSourceUrl(request.getSourceUrl());
        }
        if (request.getImageUrl() != null) {
            news.setImageUrl(request.getImageUrl());
        }
        if (request.getAuthor() != null) {
            news.setAuthor(request.getAuthor());
        }
        if (request.getCategory() != null) {
            news.setCategory(request.getCategory());
        }
        if (request.getIsActive() != null) {
            news.setIsActive(request.getIsActive());
        }
    }
    
    public NewsResponse toResponse(News news) {
        return new NewsResponse(
            news.getId(),
            news.getTitle(),
            news.getContent(),
            news.getSourceUrl(),
            news.getImageUrl(),
            news.getAuthor(),
            news.getIsActive(),
            news.getCreatedAt(),
            news.getUpdatedAt(),
            news.getCategory()
        );
    }
}