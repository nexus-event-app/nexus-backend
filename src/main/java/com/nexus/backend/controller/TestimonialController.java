package com.nexus.backend.controller;

import com.nexus.backend.dto.request.CreateTestimonialRequest;
import com.nexus.backend.dto.request.UpdateTestimonialRequest;
import com.nexus.backend.dto.response.TestimonialResponse;
import com.nexus.backend.service.TestimonialService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testimonials")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TestimonialController {
    
    private final TestimonialService testimonialService;
    
    @PostMapping
    public ResponseEntity<TestimonialResponse> createTestimonial(
            @Valid @RequestBody CreateTestimonialRequest request,
            HttpServletRequest httpRequest) {
        
        String userIp = getClientIp(httpRequest);
        TestimonialResponse response = testimonialService.createTestimonial(request, userIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TestimonialResponse>> getAllActiveTestimonials() {
        List<TestimonialResponse> testimonials = testimonialService.getAllActiveTestimonials();
        return ResponseEntity.ok(testimonials);
    }
    
    @GetMapping("/latest")
    public ResponseEntity<List<TestimonialResponse>> getLatestTestimonials() {
        List<TestimonialResponse> testimonials = testimonialService.getLatestTestimonials();
        return ResponseEntity.ok(testimonials);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestimonialResponse> getTestimonialById(@PathVariable Long id) {
        TestimonialResponse testimonial = testimonialService.getTestimonialById(id);
        return ResponseEntity.ok(testimonial);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TestimonialResponse> updateTestimonial(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTestimonialRequest request) {
        TestimonialResponse response = testimonialService.updateTestimonial(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestimonial(@PathVariable Long id) {
        testimonialService.deleteTestimonial(id);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}