package com.nexus.backend.importer;

import com.nexus.backend.enums.ImportStatus;

/**
 * Resultado de ejecutar una fuente.
 *
 * @param fetched   elementos leídos del origen
 * @param imported  elementos nuevos guardados
 */
public record ImportRunResult(ImportStatus status, int fetched, int imported, String message) {

    static ImportRunResult success(int fetched, int imported) {
        String message = imported == 0
                ? "Sin novedades (" + fetched + " elementos revisados)"
                : imported + " nuevos de " + fetched + " revisados";
        return new ImportRunResult(ImportStatus.SUCCESS, fetched, imported, message);
    }

    static ImportRunResult failure(String message) {
        return new ImportRunResult(ImportStatus.FAILED, 0, 0, message);
    }
}
