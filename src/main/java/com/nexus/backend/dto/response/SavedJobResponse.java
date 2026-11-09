package com.nexus.backend.dto.response;

import java.time.LocalDateTime;

/**
 * Oferta guardada por el usuario. {@code available} es false si la oferta ya no está publicada (desactivada o
 * caducada): se sigue mostrando en su lista para que pueda quitarla, pero ya no se puede consultar.
 */
public record SavedJobResponse(JobResponse job, boolean available, LocalDateTime savedAt) {
}
