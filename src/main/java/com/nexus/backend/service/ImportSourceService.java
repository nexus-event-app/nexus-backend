package com.nexus.backend.service;

import com.nexus.backend.dto.request.ImportSourceRequest;
import com.nexus.backend.dto.response.ImportRunResponse;
import com.nexus.backend.dto.response.ImportSourceResponse;

import java.util.List;

public interface ImportSourceService {

    List<ImportSourceResponse> getAll();

    ImportSourceResponse create(ImportSourceRequest request);

    ImportSourceResponse update(Long id, ImportSourceRequest request);

    void delete(Long id);

    /** Ejecuta la fuente ahora, respetando el tiempo mínimo entre ejecuciones manuales de su tipo. */
    ImportRunResponse runNow(Long id);
}
