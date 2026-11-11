package com.nexus.backend.service;

import com.nexus.backend.dto.response.LearningProgressResponse;
import com.nexus.backend.security.UserDetail;

import java.util.List;

public interface LearningProgressService {

    List<LearningProgressResponse> getProgress(UserDetail actor);

    /** Marca una etapa como hecha. Repetirlo no cambia nada. */
    void complete(String pathId, String stepId, UserDetail actor);

    /** Quita la marca de una etapa. Si no estaba marcada no cambia nada. */
    void undo(String pathId, String stepId, UserDetail actor);
}
