package com.nexus.backend.dto.response;

import java.util.List;

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

    public record Person(Long id, String slug, String name, String headline, String avatarUrl) {
    }
}
