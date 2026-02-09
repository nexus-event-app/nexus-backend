package com.nexus.backend.repository;

import com.nexus.backend.entity.News;
import com.nexus.backend.enums.NewsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    
    List<News> findByIsActiveTrueOrderByCreatedAtDesc();
    
    List<News> findTop3ByIsActiveTrueOrderByCreatedAtDesc();
    
    List<News> findByIsActiveTrueAndCategoryOrderByCreatedAtDesc(NewsCategory category);
}