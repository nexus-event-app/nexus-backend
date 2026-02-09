package com.nexus.backend.service;

import com.nexus.backend.dto.request.CreateNewsRequest;
import com.nexus.backend.dto.request.UpdateNewsRequest;
import com.nexus.backend.dto.response.NewsResponse;
import java.util.List;
import com.nexus.backend.enums.NewsCategory;


public interface NewsService {
    
    NewsResponse createNews(CreateNewsRequest request);
    
    List<NewsResponse> getAllActiveNews();

    NewsResponse getNewsById(Long id);
    
    NewsResponse updateNews(Long id, UpdateNewsRequest request);

    void deleteNews(Long id);
    
    List<NewsResponse> getLatestNews();

    List<NewsResponse> getNewsByCategory(NewsCategory category);

}