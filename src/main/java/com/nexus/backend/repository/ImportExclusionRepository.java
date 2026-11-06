package com.nexus.backend.repository;

import com.nexus.backend.entity.ImportExclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;

public interface ImportExclusionRepository extends JpaRepository<ImportExclusion, String> {

    @Query("select e.externalId from ImportExclusion e where e.externalId in :ids")
    Set<String> findExisting(@Param("ids") Collection<String> externalIds);
}
