package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.GuideRequest;
import com.nexus.backend.dto.response.AdminGuideResponse;
import com.nexus.backend.dto.response.GuideResponse;
import com.nexus.backend.entity.Guide;
import com.nexus.backend.exception.ConflictException;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.repository.GuideRepository;
import com.nexus.backend.service.GuideService;
import com.nexus.backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuideServiceImpl implements GuideService {

    private static final String NOT_FOUND_MESSAGE = "error.guide.notFound";
    private static final String SLUG_TAKEN_MESSAGE = "error.guide.slugTaken";

    private final GuideRepository guideRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GuideResponse> getPublishedGuides() {
        return guideRepository.findAllByPublishedTrueOrderByPublishedAtDesc().stream()
                .map(guide -> GuideResponse.summary(guide, LocaleContextHolder.getLocale()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GuideResponse getPublishedGuide(String slug) {
        return guideRepository.findBySlugAndPublishedTrue(slug)
                .map(guide -> GuideResponse.full(guide, LocaleContextHolder.getLocale()))
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminGuideResponse> getAllGuides() {
        return guideRepository.findAllByOrderByUpdatedAtDesc().stream().map(AdminGuideResponse::summary).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminGuideResponse getGuide(Long id) {
        return AdminGuideResponse.full(findById(id));
    }

    @Override
    @Transactional
    public AdminGuideResponse create(GuideRequest request) {
        Guide guide = new Guide();
        apply(guide, request, slugFor(request));
        if (guideRepository.existsBySlug(guide.getSlug())) {
            throw new ConflictException(SLUG_TAKEN_MESSAGE);
        }
        return AdminGuideResponse.full(guideRepository.save(guide));
    }

    @Override
    @Transactional
    public AdminGuideResponse update(Long id, GuideRequest request) {
        Guide guide = findById(id);
        String slug = slugFor(request);
        if (guideRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ConflictException(SLUG_TAKEN_MESSAGE);
        }
        apply(guide, request, slug);
        return AdminGuideResponse.full(guideRepository.save(guide));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        guideRepository.delete(findById(id));
    }

    private static String slugFor(GuideRequest request) {
        return request.getSlug() == null || request.getSlug().isBlank()
                ? SlugUtil.slugify(request.getTitle())
                : request.getSlug();
    }

    private static void apply(Guide guide, GuideRequest request, String slug) {
        guide.setTitle(request.getTitle());
        guide.setSlug(slug);
        guide.setDescription(request.getDescription());
        guide.setContent(request.getContent());
        guide.setTitleCa(request.getTitleCa());
        guide.setDescriptionCa(request.getDescriptionCa());
        guide.setContentCa(request.getContentCa());
        guide.setTitleEn(request.getTitleEn());
        guide.setDescriptionEn(request.getDescriptionEn());
        guide.setContentEn(request.getContentEn());
        guide.setKeywords(request.getKeywords());
        guide.setKeywordsCa(request.getKeywordsCa());
        guide.setKeywordsEn(request.getKeywordsEn());
        guide.setCategory(request.getCategory());
        guide.setPublished(request.isPublished());
        if (request.isPublished() && guide.getPublishedAt() == null) {
            guide.setPublishedAt(LocalDateTime.now());
        }
    }

    private Guide findById(Long id) {
        return guideRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
    }
}
