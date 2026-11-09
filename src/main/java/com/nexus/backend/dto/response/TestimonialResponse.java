package com.nexus.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialResponse {

    private Long id;
    private String name;
    private String role;
    private String location;
    private String content;
    /** Autor con cuenta (si la tiene y sigue activa): enlaza a su perfil público. */
    private Long authorId;
    private String authorAvatarUrl;
    private Boolean isActive;
    private Boolean isPending;
    private Boolean isHighlighted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
