package com.nexus.backend.service;

import com.nexus.backend.dto.request.CreateDiscussionRequest;
import com.nexus.backend.dto.request.UpdateDiscussionRequest;
import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.enums.DiscussionCategory;
import com.nexus.backend.security.UserDetail;

public interface DiscussionService {

    /** {@code viewer} puede ser null (visitante anónimo). */
    PageResponse<DiscussionResponse> getDiscussions(DiscussionCategory category, String text, int page, int size,
                                                    UserDetail viewer);

    DiscussionResponse getDiscussionById(Long id, UserDetail viewer);

    PageResponse<DiscussionResponse> getMyDiscussions(int page, int size, UserDetail actor);

    PageResponse<DiscussionResponse> getAllDiscussions(String text, int page, int size, UserDetail actor);

    DiscussionResponse createDiscussion(CreateDiscussionRequest request, UserDetail actor);

    DiscussionResponse updateDiscussion(Long id, UpdateDiscussionRequest request, UserDetail actor);

    void deleteDiscussion(Long id, UserDetail actor);

    /** Marca un comentario como la mejor respuesta. Solo puede hacerlo quien abrió la discusión. */
    DiscussionResponse acceptAnswer(Long id, Long commentId, UserDetail actor);

    /** Quita la marca de mejor respuesta. */
    DiscussionResponse clearAcceptedAnswer(Long id, UserDetail actor);

    DiscussionResponse toggleLike(Long id, UserDetail actor);

    /** Discusiones publicadas de un autor, para su perfil público. */
    PageResponse<DiscussionResponse> getDiscussionsByAuthor(Long authorId, int page, int size, UserDetail viewer);
}
