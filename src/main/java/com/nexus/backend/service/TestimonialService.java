package com.nexus.backend.service;

import com.nexus.backend.dto.request.CreateTestimonialRequest;
import com.nexus.backend.dto.request.UpdateTestimonialRequest;
import com.nexus.backend.dto.response.TestimonialResponse;
import java.util.List;

public interface TestimonialService {

    TestimonialResponse createTestimonial(CreateTestimonialRequest request, String userIp);

    List<TestimonialResponse> getAllActiveTestimonials();

    List<TestimonialResponse> getLatestTestimonials();

    TestimonialResponse getTestimonialById(Long id);

    TestimonialResponse updateTestimonial(Long id, UpdateTestimonialRequest request);

    void deleteTestimonial(Long id);

    boolean canCreateTestimonial(String userIp);
}