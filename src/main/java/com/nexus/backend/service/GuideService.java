package com.nexus.backend.service;

import com.nexus.backend.dto.request.GuideRequest;
import com.nexus.backend.dto.response.GuideResponse;

import java.util.List;

public interface GuideService {

    List<GuideResponse> getPublishedGuides();

    GuideResponse getPublishedGuide(String slug);

    List<GuideResponse> getAllGuides();

    GuideResponse getGuide(Long id);

    GuideResponse create(GuideRequest request);

    GuideResponse update(Long id, GuideRequest request);

    void delete(Long id);
}
