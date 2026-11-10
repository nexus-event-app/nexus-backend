package com.nexus.backend.controller;

import com.nexus.backend.dto.response.SearchResponse;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Búsqueda global: noticias, ofertas, discusiones y personas. */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "" + SearchService.DEFAULT_LIMIT) int limit,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(searchService.search(q, limit, principal));
    }
}
