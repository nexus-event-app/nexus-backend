package com.nexus.backend.controller;

import com.nexus.backend.dto.request.CreateTestimonialRequest;
import com.nexus.backend.dto.response.TestimonialResponse;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.TestimonialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/testimonials")
@RequiredArgsConstructor
public class TestimonialController {

    private final TestimonialService testimonialService;

    @GetMapping
    public ResponseEntity<List<TestimonialResponse>> getPublishedTestimonials() {
        return ResponseEntity.ok(testimonialService.getPublishedTestimonials());
    }

    @GetMapping("/latest")
    public ResponseEntity<List<TestimonialResponse>> getLatestTestimonials() {
        return ResponseEntity.ok(testimonialService.getLatestTestimonials());
    }

    @PostMapping
    public ResponseEntity<TestimonialResponse> createTestimonial(
            @Valid @RequestBody CreateTestimonialRequest request,
            @AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testimonialService.createTestimonial(request, principal));
    }
}
