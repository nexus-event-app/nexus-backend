package com.nexus.backend.controller;

import com.nexus.backend.dto.response.LearningProgressResponse;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.LearningProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Progreso de la persona con sesión iniciada en las rutas de aprendizaje. */
@RestController
@RequestMapping("/api/learning/progress")
@RequiredArgsConstructor
public class LearningProgressController {

    private final LearningProgressService progressService;

    @GetMapping
    public ResponseEntity<List<LearningProgressResponse>> getProgress(@AuthenticationPrincipal UserDetail principal) {
        return ResponseEntity.ok(progressService.getProgress(principal));
    }

    @PutMapping("/{pathId}/{stepId}")
    public ResponseEntity<Void> complete(@PathVariable String pathId, @PathVariable String stepId,
                                         @AuthenticationPrincipal UserDetail principal) {
        progressService.complete(pathId, stepId, principal);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{pathId}/{stepId}")
    public ResponseEntity<Void> undo(@PathVariable String pathId, @PathVariable String stepId,
                                     @AuthenticationPrincipal UserDetail principal) {
        progressService.undo(pathId, stepId, principal);
        return ResponseEntity.noContent().build();
    }
}
