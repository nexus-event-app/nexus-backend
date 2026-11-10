package com.nexus.backend.dto.response;

import java.util.List;

/**
 * Resultado de la búsqueda global: los primeros resultados de cada sección y cuántos hay en total, para enlazar a
 * la sección con la búsqueda aplicada. Solo incluye contenido público (publicado, vigente o activo).
 */
public record SearchResponse(
        String query,
        Group<NewsResponse> news,
        Group<JobResponse> jobs,
        Group<DiscussionResponse> discussions,
        Group<Person> people) {

    public record Group<T>(long total, List<T> items) {

        public static <T> Group<T> of(PageResponse<T> page) {
            return new Group<>(page.getTotalElements(), page.getContent());
        }
    }

    /** Perfil en los resultados: solo datos públicos del perfil, nunca el email. */
    public record Person(Long id, String slug, String name, String headline, String avatarUrl) {
    }
}
