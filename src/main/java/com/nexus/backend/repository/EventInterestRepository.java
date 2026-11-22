package com.nexus.backend.repository;

import com.nexus.backend.entity.EventInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EventInterestRepository extends JpaRepository<EventInterest, EventInterest.Key> {

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    long countByEventId(Long eventId);

    @Modifying
    @Query("delete from EventInterest i where i.eventId = :eventId and i.userId = :userId")
    int deleteOne(@Param("eventId") Long eventId, @Param("userId") Long userId);

    @Query("select i.eventId, count(i) from EventInterest i where i.eventId in :eventIds group by i.eventId")
    List<Object[]> countByEventIds(@Param("eventIds") Collection<Long> eventIds);

    @Query("select i.eventId from EventInterest i where i.userId = :userId and i.eventId in :eventIds")
    List<Long> findEventIdsOfUser(@Param("userId") Long userId, @Param("eventIds") Collection<Long> eventIds);
}
