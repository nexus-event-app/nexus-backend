package com.nexus.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {

    private long users;
    private long news;
    private long jobs;
    private long discussions;
    private long comments;
    private long pendingTestimonials;
    /** Reportes de la comunidad pendientes de revisar. */
    private long pendingReports;
}
