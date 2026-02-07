package com.nexus.backend.repository;

import com.nexus.backend.entity.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {
    
    List<Testimonial> findByIsActiveTrueOrderByIsHighlightedDescCreatedAtDesc();
    
    List<Testimonial> findTop8ByIsActiveTrueOrderByIsHighlightedDescCreatedAtDesc();
    
    boolean existsByUserIpAndCreatedAtAfter(String userIp, LocalDateTime time);
}