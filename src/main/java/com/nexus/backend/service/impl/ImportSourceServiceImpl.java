package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.ImportSourceRequest;
import com.nexus.backend.dto.response.ImportRunResponse;
import com.nexus.backend.dto.response.ImportSourceResponse;
import com.nexus.backend.entity.ImportSource;
import com.nexus.backend.exception.BadRequestException;
import com.nexus.backend.exception.ConflictException;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.importer.DefaultImportSources;
import com.nexus.backend.importer.ImportRunResult;
import com.nexus.backend.importer.ImportRunner;
import com.nexus.backend.importer.UrlGuard;
import com.nexus.backend.importer.ImportException;
import com.nexus.backend.mapper.ImportSourceMapper;
import com.nexus.backend.repository.ImportSourceRepository;
import com.nexus.backend.service.ImportSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportSourceServiceImpl implements ImportSourceService {

    private static final String NOT_FOUND_MESSAGE = "Fuente de importación no encontrada";

    private final ImportSourceRepository sourceRepository;
    private final ImportSourceMapper sourceMapper;
    private final ImportRunner importRunner;

    @Override
    @Transactional(readOnly = true)
    public List<ImportSourceResponse> getAll() {
        return sourceRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ImportSourceResponse create(ImportSourceRequest request) {
        validate(request);
        if (sourceRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Ya existe una fuente con ese nombre");
        }
        return toResponse(sourceRepository.save(sourceMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public ImportSourceResponse update(Long id, ImportSourceRequest request) {
        validate(request);
        ImportSource source = findById(id);
        if (sourceRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new ConflictException("Ya existe una fuente con ese nombre");
        }
        if (source.getType() != request.getType()) {
            throw new BadRequestException("No se puede cambiar el tipo de una fuente; crea una nueva");
        }
        sourceMapper.updateEntity(source, request);
        return toResponse(sourceRepository.save(source));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sourceRepository.delete(findById(id));
    }

    @Override
    public ImportRunResponse runNow(Long id) {
        ImportSource source = findById(id);
        if (source.getLastRunAt() != null) {
            LocalDateTime allowedFrom = source.getLastRunAt().plusMinutes(source.getType().getManualCooldownMinutes());
            if (allowedFrom.isAfter(LocalDateTime.now())) {
                throw new BadRequestException("Esta fuente se consultó hace poco. Podrás volver a lanzarla dentro de "
                        + humanize(Duration.between(LocalDateTime.now(), allowedFrom)));
            }
        }
        ImportRunResult result = importRunner.run(source);
        return new ImportRunResponse(result.status(), result.fetched(), result.imported(), result.message(),
                toResponse(findById(id)));
    }

    static String humanize(Duration remaining) {
        long minutes = Math.max(1, (remaining.getSeconds() + 59) / 60);
        if (minutes < 60) {
            return minutes + " min";
        }
        long hours = minutes / 60;
        long rest = minutes % 60;
        return rest == 0 ? hours + " h" : hours + " h " + rest + " min";
    }

    @Override
    @Transactional
    public List<ImportSourceResponse> addMissingDefaults() {
        List<ImportSource> missing = DefaultImportSources.create().stream()
                .filter(source -> !sourceRepository.existsByNameIgnoreCase(source.getName()))
                .toList();
        return sourceRepository.saveAll(missing).stream().map(this::toResponse).toList();
    }

    private void validate(ImportSourceRequest request) {
        try {
            UrlGuard.validate(request.getUrl());
        } catch (ImportException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private ImportSource findById(Long id) {
        return sourceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
    }

    private ImportSourceResponse toResponse(ImportSource source) {
        return sourceMapper.toResponse(source, importRunner.isRunning(source.getId()));
    }
}
