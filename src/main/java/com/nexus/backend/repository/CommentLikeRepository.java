package com.nexus.backend.repository;

import com.nexus.backend.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

    long countByCommentId(Long commentId);

    @Query("select cl.comment.id from CommentLike cl "
            + "where cl.user.id = :userId and cl.comment.id in :commentIds")
    List<Long> findLikedCommentIds(@Param("userId") Long userId,
                                   @Param("commentIds") Collection<Long> commentIds);

    @Modifying
    @Query("delete from CommentLike cl where cl.comment.id = :commentId")
    void deleteAllByCommentId(@Param("commentId") Long commentId);

    @Modifying
    @Query("delete from CommentLike cl where cl.comment.id in "
            + "(select c.id from Comment c where c.discussion.id = :discussionId)")
    void deleteAllByDiscussionId(@Param("discussionId") Long discussionId);
}
