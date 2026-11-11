package com.nexus.backend.repository;

import com.nexus.backend.entity.Notification;
import com.nexus.backend.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/** Solo se muestran los avisos de discusiones publicadas: si se oculta una discusión, sus avisos desaparecen. */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = "select n from Notification n join fetch n.actor join fetch n.discussion d "
            + "where n.recipient.id = :recipientId and d.isActive = true",
            countQuery = "select count(n) from Notification n "
                    + "where n.recipient.id = :recipientId and n.discussion.isActive = true")
    Page<Notification> findVisible(@Param("recipientId") Long recipientId, Pageable pageable);

    @Query("select count(n) from Notification n "
            + "where n.recipient.id = :recipientId and n.readAt is null and n.discussion.isActive = true")
    long countUnread(@Param("recipientId") Long recipientId);

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    @Modifying
    @Query("update Notification n set n.readAt = :now where n.recipient.id = :recipientId and n.readAt is null")
    int markAllRead(@Param("recipientId") Long recipientId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("delete from Notification n where n.type = :type and n.comment.id = :commentId")
    void deleteByTypeAndCommentId(@Param("type") NotificationType type, @Param("commentId") Long commentId);
}
