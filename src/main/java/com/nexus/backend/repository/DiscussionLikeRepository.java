package com.nexus.backend.repository;

import com.nexus.backend.entity.DiscussionLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DiscussionLikeRepository extends JpaRepository<DiscussionLike, Long> {

    Optional<DiscussionLike> findByDiscussionIdAndUserId(Long discussionId, Long userId);

    long countByDiscussionId(Long discussionId);

    @Query("select dl.discussion.id from DiscussionLike dl "
            + "where dl.user.id = :userId and dl.discussion.id in :discussionIds")
    List<Long> findLikedDiscussionIds(@Param("userId") Long userId,
                                      @Param("discussionIds") Collection<Long> discussionIds);

    @Modifying
    @Query("delete from DiscussionLike dl where dl.discussion.id = :discussionId")
    void deleteAllByDiscussionId(@Param("discussionId") Long discussionId);
}
