package com.nexus.backend.service;

import com.nexus.backend.dto.request.GuideRequest;
import com.nexus.backend.dto.response.AdminGuideResponse;
import com.nexus.backend.dto.response.GuideResponse;

import java.util.List;

public interface GuideService {

    List<GuideResponse> getPublishedGuides();

    GuideResponse getPublishedGuide(String slug);

    List<AdminGuideResponse> getAllGuides();

    AdminGuideResponse getGuide(Long id);

    AdminGuideResponse create(GuideRequest request);

    AdminGuideResponse update(Long id, GuideRequest request);

    void delete(Long id);
}
