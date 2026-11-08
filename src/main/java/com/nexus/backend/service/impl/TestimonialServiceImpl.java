package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateTestimonialRequest;
import com.nexus.backend.dto.request.UpdateTestimonialRequest;
import com.nexus.backend.dto.response.TestimonialResponse;
import com.nexus.backend.entity.Testimonial;
import com.nexus.backend.entity.User;
import com.nexus.backend.exception.ConflictException;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.exception.UnauthorizedException;
import com.nexus.backend.mapper.TestimonialMapper;
import com.nexus.backend.repository.TestimonialRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.AccountService;
import com.nexus.backend.service.TestimonialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestimonialServiceImpl implements TestimonialService {

    private static final int PUBLISHED_LIMIT = 100;
    private static final int LATEST_LIMIT = 8;

    private final AccountService accountService;
    private final TestimonialRepository testimonialRepository;
    private final UserRepository userRepository;
    private final TestimonialMapper testimonialMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TestimonialResponse> getPublishedTestimonials() {
        return findPublished(PUBLISHED_LIMIT);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestimonialResponse> getLatestTestimonials() {
        return findPublished(LATEST_LIMIT);
    }

    @Override
    @Transactional
    public TestimonialResponse createTestimonial(CreateTestimonialRequest request, UserDetail actor) {
        accountService.assertCanParticipate(actor);
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        if (testimonialRepository.existsByUserIdAndCreatedAtAfter(actor.getId(), since)) {
            throw new ConflictException("error.testimonial.limit");
        }

        User user = userRepository.findById(actor.getId())
                .orElseThrow(() -> new UnauthorizedException("error.user.notFound"));
        Testimonial saved = testimonialRepository.save(testimonialMapper.toEntity(request, user));
        return testimonialMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestimonialResponse> getAllTestimonials() {
        return testimonialRepository.findAllForModeration().stream()
                .map(testimonialMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TestimonialResponse updateTestimonial(Long id, UpdateTestimonialRequest request) {
        Testimonial testimonial = findById(id);

        if (request.getIsActive() != null) {
            testimonial.setIsActive(request.getIsActive());
            if (request.getIsActive() && request.getIsPending() == null) {
                testimonial.setIsPending(false);
            }
        }
        if (request.getIsPending() != null) {
            testimonial.setIsPending(request.getIsPending());
        }
        if (request.getIsHighlighted() != null) {
            testimonial.setIsHighlighted(request.getIsHighlighted());
        }
        return testimonialMapper.toResponse(testimonialRepository.save(testimonial));
    }

    @Override
    @Transactional
    public void deleteTestimonial(Long id) {
        testimonialRepository.delete(findById(id));
    }

    private List<TestimonialResponse> findPublished(int limit) {
        return testimonialRepository.findPublished(PageRequest.of(0, limit)).stream()
                .map(testimonialMapper::toResponse)
                .toList();
    }

    private Testimonial findById(Long id) {
        return testimonialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.testimonial.notFound"));
    }
}
