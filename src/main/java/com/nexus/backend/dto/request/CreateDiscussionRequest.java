package com.nexus.backend.dto.request;

import com.nexus.backend.enums.DiscussionCategory;
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
public class CreateDiscussionRequest {

    @NotBlank(message = "{validation.title.required}")
    @Size(min = 5, max = 200, message = "{validation.title.size.5.200}")
    private String title;

    @NotBlank(message = "{validation.content.required}")
    @Size(min = 10, max = 10000, message = "{validation.content.size.10.10000}")
    private String content;

    @NotNull(message = "{validation.category.required}")
    private DiscussionCategory category;

    public void setTitle(String title) {
        this.title = TextUtil.cleanLine(title);
    }

    public void setContent(String content) {
        this.content = TextUtil.clean(content);
    }
}
