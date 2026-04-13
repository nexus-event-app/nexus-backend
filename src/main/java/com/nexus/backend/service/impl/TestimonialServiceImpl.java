package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateTestimonialRequest;
import com.nexus.backend.dto.request.UpdateTestimonialRequest;
import com.nexus.backend.dto.response.TestimonialResponse;
import com.nexus.backend.entity.Testimonial;
import com.nexus.backend.mapper.TestimonialMapper;
import com.nexus.backend.repository.TestimonialRepository;
import com.nexus.backend.service.TestimonialService;
import com.nexus.backend.util.HtmlSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestimonialServiceImpl implements TestimonialService {
    
    private final TestimonialRepository testimonialRepository;
    private final TestimonialMapper testimonialMapper;
    private final HtmlSanitizer htmlSanitizer;
    
    @Override
    @Transactional
    public TestimonialResponse createTestimonial(CreateTestimonialRequest request, String userIp) {
        if (!canCreateTestimonial(userIp)) {
            throw new RuntimeException("Solo puedes crear un testimonio cada 24 horas");
        }
        
        request.setName(htmlSanitizer.sanitize(request.getName()));
        request.setRole(request.getRole() != null ? htmlSanitizer.sanitize(request.getRole()) : null);
        request.setLocation(request.getLocation() != null ? htmlSanitizer.sanitize(request.getLocation()) : null);
        request.setContent(htmlSanitizer.sanitize(request.getContent()));
        
        Testimonial testimonial = testimonialMapper.toEntity(request, userIp);
        Testimonial savedTestimonial = testimonialRepository.save(testimonial);
        return testimonialMapper.toResponse(savedTestimonial);
    }
    
    @Override
    public List<TestimonialResponse> getAllActiveTestimonials() {
        return testimonialRepository.findByIsActiveTrueOrderByIsHighlightedDescCreatedAtDesc()
            .stream()
            .map(testimonialMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TestimonialResponse> getLatestTestimonials() {
        return testimonialRepository.findTop8ByIsActiveTrueOrderByIsHighlightedDescCreatedAtDesc()
            .stream()
            .map(testimonialMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public TestimonialResponse getTestimonialById(Long id) {
        Testimonial testimonial = testimonialRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Testimonio no encontrado con ID: " + id));
        return testimonialMapper.toResponse(testimonial);
    }
    
    @Override
    @Transactional
    public TestimonialResponse updateTestimonial(Long id, UpdateTestimonialRequest request) {
        Testimonial testimonial = testimonialRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Testimonio no encontrado con ID: " + id));
        
        request.setName(htmlSanitizer.sanitize(request.getName()));
        request.setRole(request.getRole() != null ? htmlSanitizer.sanitize(request.getRole()) : null);
        request.setLocation(request.getLocation() != null ? htmlSanitizer.sanitize(request.getLocation()) : null);
        request.setContent(htmlSanitizer.sanitize(request.getContent()));
        
        testimonialMapper.updateEntity(testimonial, request);
        Testimonial updatedTestimonial = testimonialRepository.save(testimonial);
        return testimonialMapper.toResponse(updatedTestimonial);
    }
    
    @Override
    @Transactional
    public void deleteTestimonial(Long id) {
        Testimonial testimonial = testimonialRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Testimonio no encontrado con ID: " + id));
        testimonialRepository.delete(testimonial);
    }
    
    @Override
    public boolean canCreateTestimonial(String userIp) {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        return !testimonialRepository.existsByUserIpAndCreatedAtAfter(userIp, twentyFourHoursAgo);
    }
}