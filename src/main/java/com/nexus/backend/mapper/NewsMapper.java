package com.nexus.backend.mapper;

import com.nexus.backend.dto.request.CreateNewsRequest;
import com.nexus.backend.dto.request.UpdateNewsRequest;
import com.nexus.backend.dto.response.NewsResponse;
import com.nexus.backend.entity.News;
import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.util.ContentLanguage;
import com.nexus.backend.util.SlugUtil;
import org.springframework.stereotype.Component;

@Component
public class NewsMapper {

    public News toEntity(CreateNewsRequest request) {
        News news = new News();
        applyContent(news, request);
        news.setIsActive(true);
        return news;
    }

    public void updateEntity(News news, UpdateNewsRequest request) {
        applyContent(news, request);
        if (request.getIsActive() != null) {
            news.setIsActive(request.getIsActive());
        }
    }

    public NewsResponse toResponse(News news) {
        return NewsResponse.builder()
                .id(news.getId())
                .slug(SlugUtil.slugify(news.getTitle()))
                .title(news.getTitle())
                .content(news.getContent())
                .sourceUrl(news.getSourceUrl())
                .imageUrl(news.getImageUrl())
                .author(news.getAuthor())
                .category(news.getCategory())
                .source(news.getSource())
                .language(news.getLanguage())
                .imported(news.getExternalId() != null)
                .isActive(news.getIsActive())
                .createdAt(news.getCreatedAt())
                .updatedAt(news.getUpdatedAt())
                .build();
    }

    private void applyContent(News news, CreateNewsRequest request) {
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setSourceUrl(request.getSourceUrl());
        news.setImageUrl(request.getImageUrl());
        news.setAuthor(request.getAuthor());
        news.setCategory(request.getCategory() != null ? request.getCategory() : NewsCategory.GENERAL);
        news.setLanguage(ContentLanguage.orDefault(request.getLanguage()));
    }
}
