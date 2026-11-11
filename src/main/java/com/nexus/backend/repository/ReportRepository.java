package com.nexus.backend.repository;

import com.nexus.backend.entity.Report;
import com.nexus.backend.enums.ReportAction;
import com.nexus.backend.enums.ReportStatus;
import com.nexus.backend.enums.ReportTargetType;
import com.nexus.backend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    /** Contenido reportado, uno por fila: el que más reportes tiene y el más reciente primero. */
    interface ReportedTarget {
        ReportTargetType getTargetType();

        Long getTargetId();

        long getTotal();

        LocalDateTime getLastReportedAt();
    }

    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, Long targetId);

    @Query("select r.targetType as targetType, r.targetId as targetId, count(r) as total, "
            + "max(r.createdAt) as lastReportedAt from Report r where r.status in :statuses "
            + "group by r.targetType, r.targetId order by count(r) desc, max(r.createdAt) desc")
    List<ReportedTarget> findReportedTargets(@Param("statuses") Collection<ReportStatus> statuses, Pageable pageable);

    @Query("select r from Report r join fetch r.reporter where r.targetType = :type and r.targetId = :id "
            + "and r.status in :statuses order by r.createdAt desc")
    List<Report> findByTarget(@Param("type") ReportTargetType type, @Param("id") Long id,
                              @Param("statuses") Collection<ReportStatus> statuses);

    long countByStatus(ReportStatus status);

    @Modifying
    @Query("update Report r set r.status = :status, r.resolution = :resolution, r.resolvedBy = :admin, "
            + "r.resolvedAt = :now where r.targetType = :type and r.targetId = :id "
            + "and r.status = com.nexus.backend.enums.ReportStatus.PENDING")
    int closePending(@Param("type") ReportTargetType type, @Param("id") Long id, @Param("status") ReportStatus status,
                     @Param("resolution") ReportAction resolution, @Param("admin") User admin,
                     @Param("now") LocalDateTime now);

    /** Al borrar una discusión se borran sus comentarios: sus reportes pendientes se cierran como resueltos. */
    @Modifying
    @Query("update Report r set r.status = com.nexus.backend.enums.ReportStatus.RESOLVED, r.resolution = com.nexus.backend.enums.ReportAction.DELETE, "
            + "r.resolvedBy = :admin, r.resolvedAt = :now where r.status = com.nexus.backend.enums.ReportStatus.PENDING "
            + "and r.targetType = com.nexus.backend.enums.ReportTargetType.COMMENT "
            + "and r.targetId in (select c.id from Comment c where c.discussion.id = :discussionId)")
    void closePendingCommentReportsOfDiscussion(@Param("discussionId") Long discussionId, @Param("admin") User admin,
                                                @Param("now") LocalDateTime now);
}
