package com.nexus.backend.repository;

import com.nexus.backend.entity.Discussion;
import com.nexus.backend.enums.DiscussionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
    
    List<Discussion> findByIsActiveTrueOrderByCreatedAtDesc();
    
    List<Discussion> findByIsActiveTrueAndCategoryOrderByCreatedAtDesc(DiscussionCategory category);
    
    List<Discussion> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}