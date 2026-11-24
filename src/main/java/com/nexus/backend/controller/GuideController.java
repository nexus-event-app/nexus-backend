package com.nexus.backend.controller;

import com.nexus.backend.dto.response.GuideResponse;
import com.nexus.backend.service.GuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/guides")
@RequiredArgsConstructor
public class GuideController {

    private final GuideService guideService;

    @GetMapping
    public ResponseEntity<List<GuideResponse>> getGuides() {
        return ResponseEntity.ok(guideService.getPublishedGuides());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<GuideResponse> getGuide(@PathVariable String slug) {
        return ResponseEntity.ok(guideService.getPublishedGuide(slug));
    }
}
