package com.nexus.backend.dto.request;

import com.nexus.backend.util.TextUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {

    @NotNull(message = "{validation.discussionId.required}")
    private Long discussionId;

    @NotBlank(message = "{validation.content.required}")
    @Size(min = 3, max = 5000, message = "{validation.comment.size}")
    private String content;

    public void setContent(String content) {
        this.content = TextUtil.clean(content);
    }
}
