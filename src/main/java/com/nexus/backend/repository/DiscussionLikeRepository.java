package com.nexus.backend.repository;

import com.nexus.backend.entity.DiscussionLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscussionLikeRepository extends JpaRepository<DiscussionLike, Long> {
    
    Optional<DiscussionLike> findByDiscussionIdAndUserId(Long discussionId, Long userId);
    
    boolean existsByDiscussionIdAndUserId(Long discussionId, Long userId);
    
    void deleteByDiscussionIdAndUserId(Long discussionId, Long userId);
    
    @Query("SELECT dl.discussion.id FROM DiscussionLike dl WHERE dl.user.id = :userId")
    List<Long> findDiscussionIdsLikedByUser(Long userId);
}