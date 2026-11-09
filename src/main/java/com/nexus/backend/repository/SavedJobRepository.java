package com.nexus.backend.repository;

import com.nexus.backend.entity.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    long countByUserId(Long userId);

    @EntityGraph(attributePaths = "job")
    Page<SavedJob> findByUserId(Long userId, Pageable pageable);

    @Query("select s.job.id from SavedJob s where s.user.id = :userId order by s.createdAt desc, s.id desc")
    List<Long> findJobIdsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from SavedJob s where s.user.id = :userId and s.job.id = :jobId")
    int deleteByUserIdAndJobId(@Param("userId") Long userId, @Param("jobId") Long jobId);

    @Modifying
    @Query("delete from SavedJob s where s.job.id = :jobId")
    void deleteAllByJobId(@Param("jobId") Long jobId);
}
