package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateNewsRequest;
import com.nexus.backend.dto.request.UpdateNewsRequest;
import com.nexus.backend.dto.response.NewsResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.entity.ImportExclusion;
import com.nexus.backend.entity.News;
import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.importer.ArticleImageFinder;
import com.nexus.backend.mapper.NewsMapper;
import com.nexus.backend.repository.ImportExclusionRepository;
import com.nexus.backend.repository.NewsRepository;
import com.nexus.backend.repository.spec.NewsSpecifications;
import com.nexus.backend.service.NewsService;
import com.nexus.backend.util.ContentLanguage;
import com.nexus.backend.util.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private static final String NOT_FOUND_MESSAGE = "error.news.notFound";

    private final NewsRepository newsRepository;
    private final ImportExclusionRepository importExclusionRepository;
    private final NewsMapper newsMapper;
    private final ArticleImageFinder articleImageFinder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NewsResponse> getPublicNews(NewsCategory category, String text, int page, int size) {
        return getPublicNews(category, text, null, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NewsResponse> getPublicNews(NewsCategory category, String text, String language, int page,
                                                    int size) {
        Page<News> result = newsRepository.findAll(
                NewsSpecifications.isActive()
                        .and(NewsSpecifications.filter(category, text))
                        .and(NewsSpecifications.inLanguage(ContentLanguage.orNull(language))),
                Pagination.newestFirst(page, size));
        return PageResponse.from(result.map(newsMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsResponse> getLatestNews(int limit) {
        // La portada da prioridad al castellano: solo si no hay bastantes se completa con las de otros idiomas.
        Pageable latest = Pagination.latest(limit);
        List<News> spanish = newsRepository.findAll(
                NewsSpecifications.isActive().and(NewsSpecifications.inLanguage(ContentLanguage.SPANISH)), latest)
                .getContent();
        List<News> result = new ArrayList<>(spanish);
        if (result.size() < latest.getPageSize()) {
            newsRepository.findAll(NewsSpecifications.isActive()
                                    .and(Specification.not(NewsSpecifications.inLanguage(ContentLanguage.SPANISH))),
                            Pagination.latest(latest.getPageSize() - result.size()))
                    .getContent()
                    .forEach(result::add);
        }
        return result.stream().map(newsMapper::toResponse).toList();
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
        News news = newsMapper.toEntity(request);
        fillImageFromSource(news);
        return newsMapper.toResponse(newsRepository.save(news));
    }

    @Override
    @Transactional
    public NewsResponse updateNews(Long id, UpdateNewsRequest request) {
        News news = findById(id);
        newsMapper.updateEntity(news, request);
        fillImageFromSource(news);
        return newsMapper.toResponse(newsRepository.save(news));
    }

    /** Sin imagen subida pero con enlace a la fuente: se usa la portada de esa página, si la tiene. */
    private void fillImageFromSource(News news) {
        if ((news.getImageUrl() == null || news.getImageUrl().isBlank()) && news.getSourceUrl() != null) {
            news.setImageUrl(articleImageFinder.find(news.getSourceUrl()).orElse(null));
        }
    }

    @Override
    @Transactional
    public void deleteNews(Long id) {
        News news = findById(id);
        if (news.getExternalId() != null) {
            importExclusionRepository.save(new ImportExclusion(news.getExternalId()));
        }
        newsRepository.delete(news);
    }

    private News findById(Long id) {
        return newsRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
    }
}
