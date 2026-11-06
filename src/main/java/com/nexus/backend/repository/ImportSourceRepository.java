package com.nexus.backend.repository;

import com.nexus.backend.entity.ImportSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportSourceRepository extends JpaRepository<ImportSource, Long> {

    List<ImportSource> findAllByOrderByNameAsc();

    List<ImportSource> findByActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
