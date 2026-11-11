package com.nexus.backend.service.impl;

import com.nexus.backend.dto.response.StatsResponse;
import com.nexus.backend.repository.CommentRepository;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.JobOfferRepository;
import com.nexus.backend.repository.NewsRepository;
import com.nexus.backend.repository.TestimonialRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.service.StatsService;
import com.nexus.backend.enums.ReportStatus;
import com.nexus.backend.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final UserRepository userRepository;
    private final NewsRepository newsRepository;
    private final JobOfferRepository jobOfferRepository;
    private final DiscussionRepository discussionRepository;
    private final CommentRepository commentRepository;
    private final TestimonialRepository testimonialRepository;
    private final ReportRepository reportRepository;

    @Override
    @Transactional(readOnly = true)
    public StatsResponse getStats() {
        return StatsResponse.builder()
                .users(userRepository.count())
                .news(newsRepository.count())
                .jobs(jobOfferRepository.count())
                .discussions(discussionRepository.count())
                .comments(commentRepository.count())
                .pendingTestimonials(testimonialRepository.countPending())
                .pendingReports(reportRepository.countByStatus(ReportStatus.PENDING))
                .build();
    }
}
