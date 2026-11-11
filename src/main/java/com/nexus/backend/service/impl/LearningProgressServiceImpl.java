package com.nexus.backend.service.impl;

import com.nexus.backend.dto.response.LearningProgressResponse;
import com.nexus.backend.entity.LearningProgress;
import com.nexus.backend.exception.BadRequestException;
import com.nexus.backend.exception.UnauthorizedException;
import com.nexus.backend.repository.LearningProgressRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.LearningProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Progreso en las rutas de aprendizaje. El contenido de las rutas está en el frontend, así que aquí no se comprueba
 * que la etapa exista: solo que el identificador tenga un formato válido y que nadie guarde marcas sin límite.
 */
@Service
@RequiredArgsConstructor
public class LearningProgressServiceImpl implements LearningProgressService {

    private static final Pattern ID = Pattern.compile("[a-z0-9](?:[a-z0-9-]{0,38}[a-z0-9])?");
    /** Muy por encima de lo que suman todas las rutas: frena el uso indebido sin limitar a nadie. */
    static final int MAX_STEPS_PER_USER = 300;

    private final LearningProgressRepository progressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LearningProgressResponse> getProgress(UserDetail actor) {
        return progressRepository.findByUserIdOrderByCompletedAtAsc(actor.getId()).stream()
                .map(step -> new LearningProgressResponse(step.getPathId(), step.getStepId(), step.getCompletedAt()))
                .toList();
    }

    @Override
    @Transactional
    public void complete(String pathId, String stepId, UserDetail actor) {
        assertValid(pathId, stepId);
        if (progressRepository.existsByUserIdAndPathIdAndStepId(actor.getId(), pathId, stepId)) {
            return;
        }
        if (progressRepository.countByUserId(actor.getId()) >= MAX_STEPS_PER_USER) {
            throw new BadRequestException("error.learning.tooManySteps");
        }
        LearningProgress step = new LearningProgress();
        step.setUser(userRepository.findById(actor.getId())
                .orElseThrow(() -> new UnauthorizedException("error.user.notFound")));
        step.setPathId(pathId);
        step.setStepId(stepId);
        progressRepository.save(step);
    }

    @Override
    @Transactional
    public void undo(String pathId, String stepId, UserDetail actor) {
        assertValid(pathId, stepId);
        progressRepository.deleteStep(actor.getId(), pathId, stepId);
    }

    private static void assertValid(String pathId, String stepId) {
        if (!ID.matcher(pathId).matches() || !ID.matcher(stepId).matches()) {
            throw new BadRequestException("error.learning.invalidStep");
        }
    }
}
