package com.nexus.backend.service;

import com.nexus.backend.dto.response.SearchResponse;
import com.nexus.backend.security.UserDetail;

public interface SearchService {

    /** Longitud mínima de la búsqueda: con menos, la respuesta va vacía. */
    int MIN_QUERY_LENGTH = 2;
    int MAX_QUERY_LENGTH = 100;
    int DEFAULT_LIMIT = 5;
    int MAX_LIMIT = 10;

    SearchResponse search(String query, int limit, UserDetail viewer);
}
