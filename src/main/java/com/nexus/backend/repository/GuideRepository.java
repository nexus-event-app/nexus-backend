package com.nexus.backend.repository;

import com.nexus.backend.entity.Guide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuideRepository extends JpaRepository<Guide, Long> {

    Optional<Guide> findBySlugAndPublishedTrue(String slug);

    List<Guide> findAllByPublishedTrueOrderByPublishedAtDesc();

    List<Guide> findAllByOrderByUpdatedAtDesc();

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
