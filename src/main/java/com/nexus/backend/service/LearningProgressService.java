package com.nexus.backend.service;

import com.nexus.backend.dto.response.LearningProgressResponse;
import com.nexus.backend.security.UserDetail;

import java.util.List;

public interface LearningProgressService {

    List<LearningProgressResponse> getProgress(UserDetail actor);

    void complete(String pathId, String stepId, UserDetail actor);

    void undo(String pathId, String stepId, UserDetail actor);
}
