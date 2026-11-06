package com.nexus.backend.repository;

import com.nexus.backend.entity.JobOffer;
import com.nexus.backend.repository.projection.SitemapEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JobOfferRepository extends JpaRepository<JobOffer, Long>, JpaSpecificationExecutor<JobOffer> {

    @Query("select j.id as id, j.title as title, j.updatedAt as lastModified from JobOffer j "
            + "where j.isActive = true and (j.expiresAt is null or j.expiresAt > :currentTime) order by j.updatedAt desc")
    List<SitemapEntry> findSitemapEntries(@Param("currentTime") LocalDateTime now, Pageable pageable);
}
