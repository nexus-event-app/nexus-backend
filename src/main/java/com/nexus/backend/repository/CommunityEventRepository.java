package com.nexus.backend.repository;

import com.nexus.backend.entity.CommunityEvent;
import com.nexus.backend.enums.EventMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface CommunityEventRepository extends JpaRepository<CommunityEvent, Long> {

    /** Próximos eventos visibles, del más cercano al más lejano; forWomen y mode son opcionales (null: todos). */
    @Query("select e from CommunityEvent e where e.active = true and e.startsOn >= :today "
            + "and (:forWomen is null or e.forWomen = :forWomen) "
            + "and (:mode is null or e.mode = :mode) "
            + "order by e.startsOn asc, e.startTime asc, e.id asc")
    Page<CommunityEvent> findUpcoming(@Param("today") LocalDate today, @Param("forWomen") Boolean forWomen,
                                      @Param("mode") EventMode mode, Pageable pageable);

    /** Panel: todos, también ocultos y pasados, los más recientes primero. */
    @EntityGraph(attributePaths = "author")
    @Query("select e from CommunityEvent e order by e.startsOn desc, e.id desc")
    Page<CommunityEvent> findAllForModeration(Pageable pageable);

    long countByAuthorIdAndCreatedAtAfter(Long authorId, LocalDateTime since);

    boolean existsByUrlAndStartsOnAndActiveTrue(String url, LocalDate startsOn);

    @Modifying
    @Query("delete from CommunityEvent e where e.startsOn < :before")
    int deletePastBefore(@Param("before") LocalDate before);
}
