package com.nexus.backend.controller;

import com.nexus.backend.dto.request.ImportSourceRequest;
import com.nexus.backend.dto.response.ImportRunResponse;
import com.nexus.backend.dto.response.ImportSourceResponse;
import com.nexus.backend.service.ImportSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/import-sources")
@RequiredArgsConstructor
public class AdminImportSourceController {

    private final ImportSourceService importSourceService;

    @GetMapping
    public ResponseEntity<List<ImportSourceResponse>> getAll() {
        return ResponseEntity.ok(importSourceService.getAll());
    }

    @PostMapping
    public ResponseEntity<ImportSourceResponse> create(@Valid @RequestBody ImportSourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(importSourceService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImportSourceResponse> update(@PathVariable Long id, @Valid @RequestBody ImportSourceRequest request) {
        return ResponseEntity.ok(importSourceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        importSourceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Añade las fuentes recomendadas que falten; responde con las creadas (vacío si ya estaban todas). */
    @PostMapping("/defaults")
    public ResponseEntity<List<ImportSourceResponse>> addMissingDefaults() {
        return ResponseEntity.ok(importSourceService.addMissingDefaults());
    }

    @PostMapping("/{id}/run")
    public ResponseEntity<ImportRunResponse> run(@PathVariable Long id) {
        return ResponseEntity.ok(importSourceService.runNow(id));
    }
}
