package com.nexus.backend.controller;

import com.nexus.backend.dto.response.NewsResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.service.NewsService;
import com.nexus.backend.util.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<PageResponse<NewsResponse>> getNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_SIZE) int size,
            @RequestParam(required = false) NewsCategory category,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(newsService.getPublicNews(category, q, page, size));
    }

    @GetMapping("/latest")
    public ResponseEntity<List<NewsResponse>> getLatestNews(
            @RequestParam(defaultValue = "" + Pagination.DEFAULT_LATEST) int limit) {
        return ResponseEntity.ok(newsService.getLatestNews(limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsResponse> getNewsById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getPublicNewsById(id));
    }
}
