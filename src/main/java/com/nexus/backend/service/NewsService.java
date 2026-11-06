package com.nexus.backend.service;

import com.nexus.backend.dto.request.CreateNewsRequest;
import com.nexus.backend.dto.request.UpdateNewsRequest;
import com.nexus.backend.dto.response.NewsResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.enums.NewsCategory;

import java.util.List;

public interface NewsService {

    PageResponse<NewsResponse> getPublicNews(NewsCategory category, String text, int page, int size);

    List<NewsResponse> getLatestNews(int limit);

    NewsResponse getPublicNewsById(Long id);

    PageResponse<NewsResponse> getAllNews(NewsCategory category, String text, int page, int size);

    NewsResponse createNews(CreateNewsRequest request);

    NewsResponse updateNews(Long id, UpdateNewsRequest request);

    void deleteNews(Long id);
}
