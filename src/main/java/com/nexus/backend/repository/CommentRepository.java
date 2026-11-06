package com.nexus.backend.repository;

import com.nexus.backend.entity.Comment;
import com.nexus.backend.repository.projection.DiscussionCommentCount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = "author")
    List<Comment> findByDiscussionIdOrderByCreatedAtAsc(Long discussionId);

    long countByDiscussionId(Long discussionId);

    @Query("select c.discussion.id as discussionId, count(c) as total from Comment c "
            + "where c.discussion.id in :discussionIds group by c.discussion.id")
    List<DiscussionCommentCount> countByDiscussionIds(@Param("discussionIds") Collection<Long> discussionIds);

    @Modifying
    @Query("delete from Comment c where c.discussion.id = :discussionId")
    void deleteAllByDiscussionId(@Param("discussionId") Long discussionId);

    @Modifying
    @Query("update Comment c set c.likesCount = :likes where c.id = :id")
    void updateLikesCount(@Param("id") Long id, @Param("likes") int likes);
}
