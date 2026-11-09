package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.CreateJobRequest;
import com.nexus.backend.dto.request.UpdateJobRequest;
import com.nexus.backend.dto.response.JobResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.entity.ImportExclusion;
import com.nexus.backend.entity.JobOffer;
import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.WorkMode;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.mapper.JobMapper;
import com.nexus.backend.repository.ImportExclusionRepository;
import com.nexus.backend.repository.JobOfferRepository;
import com.nexus.backend.repository.SavedJobRepository;
import com.nexus.backend.repository.spec.JobSpecifications;
import com.nexus.backend.service.JobService;
import com.nexus.backend.util.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private static final String NOT_FOUND_MESSAGE = "error.job.notFound";

    private final JobOfferRepository jobOfferRepository;
    private final ImportExclusionRepository importExclusionRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobMapper jobMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getPublicJobs(JobLevel level, WorkMode workMode, ContractType contractType,
                                                   String text, int page, int size) {
        Page<JobOffer> result = jobOfferRepository.findAll(
                JobSpecifications.isPublished().and(JobSpecifications.filter(level, workMode, contractType, text)),
                Pagination.spainFirst(page, size));
        return PageResponse.from(result.map(jobMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponse> getLatestJobs(int limit) {
        return jobOfferRepository.findAll(JobSpecifications.isPublished(), Pagination.latestSpainFirst(limit))
                .getContent()
                .stream()
                .map(jobMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getPublicJobById(Long id) {
        JobOffer job = findById(id);
        boolean expired = job.getExpiresAt() != null && !job.getExpiresAt().isAfter(LocalDateTime.now());
        if (!Boolean.TRUE.equals(job.getIsActive()) || expired) {
            throw new ResourceNotFoundException(NOT_FOUND_MESSAGE);
        }
        return jobMapper.toResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getAllJobs(JobLevel level, WorkMode workMode, ContractType contractType,
                                                String text, int page, int size) {
        Page<JobOffer> result = jobOfferRepository.findAll(
                JobSpecifications.filter(level, workMode, contractType, text),
                Pagination.newestPublishedFirst(page, size));
        return PageResponse.from(result.map(jobMapper::toResponse));
    }

    @Override
    @Transactional
    public JobResponse createJob(CreateJobRequest request) {
        JobOffer saved = jobOfferRepository.save(jobMapper.toEntity(request));
        return jobMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public JobResponse updateJob(Long id, UpdateJobRequest request) {
        JobOffer job = findById(id);
        jobMapper.updateEntity(job, request);
        return jobMapper.toResponse(jobOfferRepository.save(job));
    }

    @Override
    @Transactional
    public void deleteJob(Long id) {
        JobOffer job = findById(id);
        if (job.getExternalId() != null) {
            importExclusionRepository.save(new ImportExclusion(job.getExternalId()));
        }
        savedJobRepository.deleteAllByJobId(id);
        jobOfferRepository.delete(job);
    }

    private JobOffer findById(Long id) {
        return jobOfferRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
    }
}
