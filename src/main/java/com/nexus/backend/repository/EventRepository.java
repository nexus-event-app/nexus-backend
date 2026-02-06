package com.nexus.backend.repository;

import com.nexus.backend.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByIsActiveTrue();
    
    List<Event> findByLocation(String location);
    
    List<Event> findByEventType(String eventType);
    
    List<Event> findByEventDateAfterAndIsActiveTrue(LocalDateTime date);
    
    List<Event> findByEventDateBeforeAndIsActiveTrue(LocalDateTime date);
    
    List<Event> findByLevelAndIsActiveTrue(String level);

    List<Event> findTop3ByIsActiveTrueOrderByCreatedAtDesc();

}