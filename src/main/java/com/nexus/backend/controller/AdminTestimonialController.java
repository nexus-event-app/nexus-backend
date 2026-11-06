package com.nexus.backend.controller;

import com.nexus.backend.dto.request.UpdateTestimonialRequest;
import com.nexus.backend.dto.response.TestimonialResponse;
import com.nexus.backend.service.TestimonialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/testimonials")
@RequiredArgsConstructor
public class AdminTestimonialController {

    private final TestimonialService testimonialService;

    @GetMapping
    public ResponseEntity<List<TestimonialResponse>> getAllTestimonials() {
        return ResponseEntity.ok(testimonialService.getAllTestimonials());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestimonialResponse> updateTestimonial(
            @PathVariable Long id,
            @RequestBody UpdateTestimonialRequest request) {
        return ResponseEntity.ok(testimonialService.updateTestimonial(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestimonial(@PathVariable Long id) {
        testimonialService.deleteTestimonial(id);
        return ResponseEntity.noContent().build();
    }
}
