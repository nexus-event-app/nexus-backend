package com.nexus.backend.service.impl;

import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.dto.response.SavedJobResponse;
import com.nexus.backend.entity.JobOffer;
import com.nexus.backend.entity.SavedJob;
import com.nexus.backend.exception.ConflictException;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.mapper.JobMapper;
import com.nexus.backend.repository.JobOfferRepository;
import com.nexus.backend.repository.SavedJobRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.SavedJobService;
import com.nexus.backend.util.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedJobServiceImpl implements SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobOfferRepository jobOfferRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SavedJobResponse> getSavedJobs(int page, int size, UserDetail actor) {
        LocalDateTime now = LocalDateTime.now();
        return PageResponse.from(savedJobRepository.findByUserId(actor.getId(), Pagination.newestFirst(page, size))
                .map(saved -> new SavedJobResponse(
                        jobMapper.toResponse(saved.getJob()), saved.getJob().isPublishedAt(now), saved.getCreatedAt())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getSavedJobIds(UserDetail actor) {
        return savedJobRepository.findJobIdsByUserId(actor.getId());
    }

    @Override
    @Transactional
    public void save(Long jobId, UserDetail actor) {
        if (savedJobRepository.existsByUserIdAndJobId(actor.getId(), jobId)) {
            return;
        }
        JobOffer job = jobOfferRepository.findById(jobId)
                .filter(offer -> offer.isPublishedAt(LocalDateTime.now()))
                .orElseThrow(() -> new ResourceNotFoundException("error.job.notFound"));
        if (!actor.isAdmin() && savedJobRepository.countByUserId(actor.getId()) >= MAX_SAVED) {
            throw new ConflictException("error.savedJob.limit");
        }
        savedJobRepository.save(new SavedJob(userRepository.getReferenceById(actor.getId()), job));
    }

    @Override
    @Transactional
    public void remove(Long jobId, UserDetail actor) {
        savedJobRepository.deleteByUserIdAndJobId(actor.getId(), jobId);
    }
}
