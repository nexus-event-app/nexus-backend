package com.nexus.backend.mapper;

import com.nexus.backend.dto.request.CreateTestimonialRequest;
import com.nexus.backend.dto.request.UpdateTestimonialRequest;
import com.nexus.backend.dto.response.TestimonialResponse;
import com.nexus.backend.entity.Testimonial;
import org.springframework.stereotype.Component;

@Component
public class TestimonialMapper {
    
    public Testimonial toEntity(CreateTestimonialRequest request, String userIp) {
        Testimonial testimonial = new Testimonial();
        testimonial.setName(request.getName());
        testimonial.setRole(request.getRole());
        testimonial.setLocation(request.getLocation());
        testimonial.setContent(request.getContent());
        testimonial.setUserIp(userIp);
        testimonial.setIsActive(true);
        testimonial.setIsPending(false);
        testimonial.setIsHighlighted(false);
        return testimonial;
    }
    
    public void updateEntity(Testimonial testimonial, UpdateTestimonialRequest request) {
        if (request.getIsActive() != null) {
            testimonial.setIsActive(request.getIsActive());
        }
        if (request.getIsPending() != null) {
            testimonial.setIsPending(request.getIsPending());
        }
        if (request.getIsHighlighted() != null) {
            testimonial.setIsHighlighted(request.getIsHighlighted());
        }
    }
    
    public TestimonialResponse toResponse(Testimonial testimonial) {
        return new TestimonialResponse(
            testimonial.getId(),
            testimonial.getName(),
            testimonial.getRole(),
            testimonial.getLocation(),
            testimonial.getContent(),
            testimonial.getIsActive(),
            testimonial.getIsPending(),
            testimonial.getIsHighlighted(),
            testimonial.getCreatedAt(),
            testimonial.getUpdatedAt()
        );
    }
}