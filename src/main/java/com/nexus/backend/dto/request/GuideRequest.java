package com.nexus.backend.dto.request;

import com.nexus.backend.util.TextUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GuideRequest {

    @NotBlank(message = "{validation.title.required}")
    @Size(max = 200, message = "{validation.title.max200}")
    private String title;

    @Size(max = 120, message = "{validation.guide.slug.max120}")
    @Pattern(regexp = "^$|^[a-z0-9]+(-[a-z0-9]+)*$", message = "{validation.guide.slug.pattern}")
    private String slug;

    @NotBlank(message = "{validation.guide.description.required}")
    @Size(max = 300, message = "{validation.guide.description.max300}")
    private String description;

    @NotBlank(message = "{validation.content.required}")
    @Size(max = 50000, message = "{validation.guide.content.max50000}")
    private String content;

    private boolean published;

    public void setTitle(String title) {
        this.title = TextUtil.cleanLine(title);
    }

    public void setSlug(String slug) {
        this.slug = slug == null ? null : slug.trim();
    }

    public void setDescription(String description) {
        this.description = TextUtil.cleanLine(description);
    }

    public void setContent(String content) {
        this.content = TextUtil.clean(content);
    }
}
