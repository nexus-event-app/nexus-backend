package com.nexus.backend.dto.response;

import com.nexus.backend.enums.DiscussionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionResponse {

    private Long id;
    private String slug;
    private String title;
    private String content;
    private DiscussionCategory category;
    private Long authorId;
    private String authorName;
    private Boolean isActive;
    private Integer commentCount;
    private Integer likesCount;
    private Boolean isLikedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
