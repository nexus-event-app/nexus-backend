package com.nexus.backend.repository;

import com.nexus.backend.entity.Discussion;
import com.nexus.backend.repository.projection.SitemapEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiscussionRepository extends JpaRepository<Discussion, Long>, JpaSpecificationExecutor<Discussion> {

    @Override
    @EntityGraph(attributePaths = "author")
    Page<Discussion> findAll(Specification<Discussion> spec, Pageable pageable);

    @Modifying
    @Query("update Discussion d set d.likesCount = :likes where d.id = :id")
    void updateLikesCount(@Param("id") Long id, @Param("likes") int likes);

    @Query("select d.id as id, d.title as title, d.updatedAt as lastModified "
            + "from Discussion d where d.isActive = true order by d.updatedAt desc")
    List<SitemapEntry> findSitemapEntries(Pageable pageable);
}
