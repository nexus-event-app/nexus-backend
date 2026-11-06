package com.nexus.backend.mapper;

import com.nexus.backend.dto.request.CreateTestimonialRequest;
import com.nexus.backend.dto.response.TestimonialResponse;
import com.nexus.backend.entity.Testimonial;
import com.nexus.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TestimonialMapper {

    /** Los testimonios nacen pendientes de moderación y sin publicar. */
    public Testimonial toEntity(CreateTestimonialRequest request, User user) {
        Testimonial testimonial = new Testimonial();
        testimonial.setName(request.getName());
        testimonial.setRole(request.getRole());
        testimonial.setLocation(request.getLocation());
        testimonial.setContent(request.getContent());
        testimonial.setUser(user);
        testimonial.setIsPending(true);
        testimonial.setIsActive(false);
        testimonial.setIsHighlighted(false);
        return testimonial;
    }

    public TestimonialResponse toResponse(Testimonial testimonial) {
        return TestimonialResponse.builder()
                .id(testimonial.getId())
                .name(testimonial.getName())
                .role(testimonial.getRole())
                .location(testimonial.getLocation())
                .content(testimonial.getContent())
                .isActive(testimonial.getIsActive())
                .isPending(testimonial.getIsPending())
                .isHighlighted(testimonial.getIsHighlighted())
                .createdAt(testimonial.getCreatedAt())
                .updatedAt(testimonial.getUpdatedAt())
                .build();
    }
}
