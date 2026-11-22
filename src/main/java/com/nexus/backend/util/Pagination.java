package com.nexus.backend.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Pagination {

    public static final int DEFAULT_SIZE = 12;
    public static final int MAX_SIZE = 50;
    public static final int DEFAULT_LATEST = 3;
    public static final int MAX_LATEST = 12;

    private Pagination() {
    }

    public static Pageable newestFirst(int page, int size) {
        return page(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
    }

    public static Pageable spainFirst(int page, int size) {
        Sort sort = Sort.by(Sort.Order.desc("inSpain"), Sort.Order.desc("publishedAt"), Sort.Order.desc("id"));
        return page(page, size, sort);
    }

    public static Pageable newestPublishedFirst(int page, int size) {
        return page(page, size, Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id")));
    }

    public static Pageable latestSpainFirst(int limit) {
        return spainFirst(0, latestSize(limit));
    }

    public static Pageable soonestFirst(int page, int size) {
        return page(page, size, Sort.by(Sort.Order.asc("startsOn"), Sort.Order.asc("startTime"), Sort.Order.asc("id")));
    }

    public static Pageable unsorted(int page, int size) {
        return page(page, size, Sort.unsorted());
    }

    public static Pageable latest(int limit) {
        return newestFirst(0, latestSize(limit));
    }

    private static Pageable page(int page, int size, Sort sort) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_SIZE), sort);
    }

    private static int latestSize(int limit) {
        return Math.min(Math.max(limit, 1), MAX_LATEST);
    }
}
