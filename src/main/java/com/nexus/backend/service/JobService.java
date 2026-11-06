package com.nexus.backend.service;

import com.nexus.backend.dto.request.CreateJobRequest;
import com.nexus.backend.dto.request.UpdateJobRequest;
import com.nexus.backend.dto.response.JobResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.WorkMode;

import java.util.List;

public interface JobService {

    PageResponse<JobResponse> getPublicJobs(JobLevel level, WorkMode workMode, ContractType contractType,
                                            String text, int page, int size);

    List<JobResponse> getLatestJobs(int limit);

    JobResponse getPublicJobById(Long id);

    PageResponse<JobResponse> getAllJobs(JobLevel level, WorkMode workMode, ContractType contractType,
                                         String text, int page, int size);

    JobResponse createJob(CreateJobRequest request);

    JobResponse updateJob(Long id, UpdateJobRequest request);

    void deleteJob(Long id);
}
