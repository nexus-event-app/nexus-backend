package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateNewsRequest;
import com.nexus.backend.dto.request.UpdateNewsRequest;
import com.nexus.backend.dto.response.NewsResponse;
import com.nexus.backend.entity.News;
import com.nexus.backend.mapper.NewsMapper;
import com.nexus.backend.repository.NewsRepository;
import com.nexus.backend.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    
    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;
    
    @Override
    @Transactional
    public NewsResponse createNews(CreateNewsRequest request) {
        News news = newsMapper.toEntity(request);
        News savedNews = newsRepository.save(news);
        return newsMapper.toResponse(savedNews);
    }
    
    @Override
    public List<NewsResponse> getAllActiveNews() {
        return newsRepository.findByIsActiveTrueOrderByCreatedAtDesc()
            .stream()
            .map(newsMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public NewsResponse getNewsById(Long id) {
        News news = newsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Noticia no encontrada con ID: " + id));
        return newsMapper.toResponse(news);
    }
    
    @Override
    @Transactional
    public NewsResponse updateNews(Long id, UpdateNewsRequest request) {
        News news = newsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Noticia no encontrada con ID: " + id));
        
        newsMapper.updateEntity(news, request);
        News updatedNews = newsRepository.save(news);
        return newsMapper.toResponse(updatedNews);
    }
    
    @Override
    @Transactional
    public void deleteNews(Long id) {
        News news = newsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Noticia no encontrada con ID: " + id));
        news.setIsActive(false);
        newsRepository.save(news);
    }
    
    @Override
    public List<NewsResponse> getLatestNews() {
        return newsRepository.findTop3ByIsActiveTrueOrderByCreatedAtDesc()
            .stream()
            .map(newsMapper::toResponse)
            .collect(Collectors.toList());
    }
}