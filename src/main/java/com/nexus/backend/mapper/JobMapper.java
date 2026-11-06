package com.nexus.backend.mapper;

import com.nexus.backend.dto.request.CreateJobRequest;
import com.nexus.backend.dto.request.UpdateJobRequest;
import com.nexus.backend.dto.response.JobResponse;
import com.nexus.backend.entity.JobOffer;
import com.nexus.backend.util.SlugUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class JobMapper {

    private static final String TAG_SEPARATOR = ",";

    public JobOffer toEntity(CreateJobRequest request) {
        JobOffer job = new JobOffer();
        applyContent(job, request);
        job.setIsActive(true);
        return job;
    }

    public void updateEntity(JobOffer job, UpdateJobRequest request) {
        applyContent(job, request);
        if (request.getIsActive() != null) {
            job.setIsActive(request.getIsActive());
        }
    }

    public JobResponse toResponse(JobOffer job) {
        return JobResponse.builder()
                .id(job.getId())
                .slug(SlugUtil.slugify(job.getTitle()))
                .title(job.getTitle())
                .company(job.getCompany())
                .description(job.getDescription())
                .location(job.getLocation())
                .workMode(job.getWorkMode())
                .contractType(job.getContractType())
                .level(job.getLevel())
                .tags(splitTags(job.getTags()))
                .applyUrl(job.getApplyUrl())
                .source(job.getSource())
                .isActive(job.getIsActive())
                .publishedAt(job.getPublishedAt())
                .expiresAt(job.getExpiresAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private void applyContent(JobOffer job, CreateJobRequest request) {
        job.setTitle(request.getTitle());
        job.setCompany(request.getCompany());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setWorkMode(request.getWorkMode());
        job.setContractType(request.getContractType());
        job.setLevel(request.getLevel());
        job.setTags(joinTags(request.getTags()));
        job.setApplyUrl(request.getApplyUrl());
        job.setSource(request.getSource());
        job.setExpiresAt(request.getExpiresAt());
    }

    private String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return String.join(TAG_SEPARATOR, tags);
    }

    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(TAG_SEPARATOR))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }
}
