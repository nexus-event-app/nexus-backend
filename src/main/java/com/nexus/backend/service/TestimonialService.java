package com.nexus.backend.service;

import com.nexus.backend.dto.request.CreateTestimonialRequest;
import com.nexus.backend.dto.request.UpdateTestimonialRequest;
import com.nexus.backend.dto.response.TestimonialResponse;
import com.nexus.backend.security.UserDetail;

import java.util.List;

public interface TestimonialService {

    List<TestimonialResponse> getPublishedTestimonials();

    List<TestimonialResponse> getLatestTestimonials();

    TestimonialResponse createTestimonial(CreateTestimonialRequest request, UserDetail actor);

    List<TestimonialResponse> getAllTestimonials();

    TestimonialResponse updateTestimonial(Long id, UpdateTestimonialRequest request);

    void deleteTestimonial(Long id);
}
