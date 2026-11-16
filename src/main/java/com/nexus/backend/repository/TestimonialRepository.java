package com.nexus.backend.repository;

import com.nexus.backend.entity.Testimonial;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Las listas cargan al autor en la misma consulta (@EntityGraph): el mapper usa su foto y si la cuenta sigue activa,
 * y sin esto haría una consulta más por cada testimonio.
 */
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {

    @EntityGraph(attributePaths = "user")
    @Query("select t from Testimonial t where t.isActive = true and t.isPending = false "
            + "order by t.isHighlighted desc, t.createdAt desc")
    List<Testimonial> findPublished(Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Query("select t from Testimonial t order by t.isPending desc, t.createdAt desc")
    List<Testimonial> findAllForModeration();

    @Query("select count(t) from Testimonial t where t.isPending = true")
    long countPending();

    @EntityGraph(attributePaths = "user")
    @Query("select t from Testimonial t where t.user.id = :userId and t.isActive = true and t.isPending = false "
            + "order by t.createdAt desc")
    List<Testimonial> findPublishedByUserId(@Param("userId") Long userId);

    @Query("select count(t) from Testimonial t where t.user.id = :userId and t.isActive = true and t.isPending = false")
    long countPublishedByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndCreatedAtAfter(Long userId, LocalDateTime since);
}
