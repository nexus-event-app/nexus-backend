package com.nexus.backend.repository;

import com.nexus.backend.entity.CommunityEvent;
import com.nexus.backend.enums.EventMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CommunityEventRepository extends JpaRepository<CommunityEvent, Long>,
        JpaSpecificationExecutor<CommunityEvent> {

    /** Panel: todos, también ocultos y pasados, los más recientes primero. */
    @EntityGraph(attributePaths = "author")
    @Query("select e from CommunityEvent e order by e.startsOn desc, e.id desc")
    Page<CommunityEvent> findAllForModeration(Pageable pageable);

    /** Calendario: los visibles entre dos fechas. */
    List<CommunityEvent> findByActiveTrueAndStartsOnBetween(LocalDate from, LocalDate to);

    /** Ciudades con eventos presenciales próximos, para el filtro. */
    @Query("select distinct e.city from CommunityEvent e where e.active = true and e.startsOn >= :today "
            + "and e.mode = :mode and e.city is not null")
    List<String> findUpcomingCities(@Param("today") LocalDate today, @Param("mode") EventMode mode);

    long countByAuthorIdAndCreatedAtAfter(Long authorId, LocalDateTime since);

    boolean existsByUrlAndStartsOnAndActiveTrue(String url, LocalDate startsOn);

    @Modifying
    @Query("delete from CommunityEvent e where e.startsOn < :before")
    int deletePastBefore(@Param("before") LocalDate before);
}
