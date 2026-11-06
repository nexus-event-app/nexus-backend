package com.nexus.backend.repository;

import com.nexus.backend.entity.News;
import com.nexus.backend.repository.projection.SitemapEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {

    @Query("select n.id as id, n.title as title, n.updatedAt as lastModified "
            + "from News n where n.isActive = true and n.externalId is null order by n.updatedAt desc")
    List<SitemapEntry> findSitemapEntries(Pageable pageable);

    @Query("select n.externalId from News n where n.externalId in :ids")
    Set<String> findExistingExternalIds(@Param("ids") Collection<String> externalIds);
}
