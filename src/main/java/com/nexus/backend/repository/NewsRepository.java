package com.nexus.backend.repository;

import com.nexus.backend.entity.News;
import com.nexus.backend.repository.projection.SitemapEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {

    @Query("select n.id as id, n.title as title, n.updatedAt as lastModified "
            + "from News n where n.isActive = true and n.externalId is null order by n.updatedAt desc")
    List<SitemapEntry> findSitemapEntries(Pageable pageable);

    @Query("select n.externalId from News n where n.externalId in :ids")
    Set<String> findExistingExternalIds(@Param("ids") Collection<String> externalIds);

    /** Noticias sin imagen pero con enlace a su fuente, de las más recientes a las más antiguas. */
    @Query("select n from News n where n.imageUrl is null and n.sourceUrl is not null and n.createdAt >= :since "
            + "and not exists (select s.id from ImportSource s where s.name = n.source and s.useSourceImages = false) "
            + "order by n.createdAt desc")
    List<News> findWithoutImage(@Param("since") LocalDateTime since, Pageable pageable);

    /** Quita las imágenes de lo importado de una fuente (cuando se decide no usar sus imágenes). */
    @Modifying
    @Query("update News n set n.imageUrl = null where n.source = :source and n.externalId is not null")
    int clearImportedImages(@Param("source") String source);

    @Modifying
    @Query("update News n set n.imageUrl = :imageUrl where n.id = :id and n.imageUrl is null")
    int updateImageUrl(@Param("id") Long id, @Param("imageUrl") String imageUrl);
}
