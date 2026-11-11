package com.nexus.backend.repository;

import com.nexus.backend.entity.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    List<LearningProgress> findByUserIdOrderByCompletedAtAsc(Long userId);

    boolean existsByUserIdAndPathIdAndStepId(Long userId, String pathId, String stepId);

    long countByUserId(Long userId);

    @Modifying
    @Query("delete from LearningProgress p where p.user.id = :userId and p.pathId = :pathId and p.stepId = :stepId")
    void deleteStep(@Param("userId") Long userId, @Param("pathId") String pathId, @Param("stepId") String stepId);
}
