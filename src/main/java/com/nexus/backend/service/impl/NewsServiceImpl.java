package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateNewsRequest;
import com.nexus.backend.dto.request.UpdateNewsRequest;
import com.nexus.backend.dto.response.NewsResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.entity.News;
import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.mapper.NewsMapper;
import com.nexus.backend.repository.NewsRepository;
import com.nexus.backend.repository.spec.NewsSpecifications;
import com.nexus.backend.service.NewsService;
import com.nexus.backend.util.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private static final String NOT_FOUND_MESSAGE = "Noticia no encontrada";

    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NewsResponse> getPublicNews(NewsCategory category, String text, int page, int size) {
        Page<News> result = newsRepository.findAll(
                NewsSpecifications.isActive().and(NewsSpecifications.filter(category, text)),
                Pagination.newestFirst(page, size));
        return PageResponse.from(result.map(newsMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsResponse> getLatestNews(int limit) {
        return newsRepository.findAll(NewsSpecifications.isActive(), Pagination.latest(limit))
                .getContent()
                .stream()
                .map(newsMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NewsResponse getPublicNewsById(Long id) {
        News news = findById(id);
        if (!Boolean.TRUE.equals(news.getIsActive())) {
            throw new ResourceNotFoundException(NOT_FOUND_MESSAGE);
        }
        return newsMapper.toResponse(news);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NewsResponse> getAllNews(NewsCategory category, String text, int page, int size) {
        Page<News> result = newsRepository.findAll(NewsSpecifications.filter(category, text),
                Pagination.newestFirst(page, size));
        return PageResponse.from(result.map(newsMapper::toResponse));
    }

    @Override
    @Transactional
    public NewsResponse createNews(CreateNewsRequest request) {
        News saved = newsRepository.save(newsMapper.toEntity(request));
        return newsMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NewsResponse updateNews(Long id, UpdateNewsRequest request) {
        News news = findById(id);
        newsMapper.updateEntity(news, request);
        return newsMapper.toResponse(newsRepository.save(news));
    }

    @Override
    @Transactional
    public void deleteNews(Long id) {
        newsRepository.delete(findById(id));
    }

    private News findById(Long id) {
        return newsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
    }
}
