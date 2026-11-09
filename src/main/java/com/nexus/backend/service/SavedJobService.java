package com.nexus.backend.service;

import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.dto.response.SavedJobResponse;
import com.nexus.backend.security.UserDetail;

import java.util.List;

public interface SavedJobService {

    /** Máximo de ofertas guardadas por usuario. */
    int MAX_SAVED = 500;

    PageResponse<SavedJobResponse> getSavedJobs(int page, int size, UserDetail actor);

    List<Long> getSavedJobIds(UserDetail actor);

    void save(Long jobId, UserDetail actor);

    void remove(Long jobId, UserDetail actor);
}
