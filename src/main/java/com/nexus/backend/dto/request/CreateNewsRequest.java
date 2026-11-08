package com.nexus.backend.dto.request;

import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.util.TextUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewsRequest {

    @NotBlank(message = "{validation.title.required}")
    @Size(max = 200, message = "{validation.title.max200}")
    private String title;

    @NotBlank(message = "{validation.content.required}")
    @Size(max = 20000, message = "{validation.content.max20000}")
    private String content;

    @Size(max = 500, message = "{validation.sourceUrl.max500}")
    @Pattern(regexp = "^https?://\\S+$", message = "{validation.sourceUrl.pattern}")
    private String sourceUrl;

    @Size(max = 500, message = "{validation.imageUrl.max500}")
    @Pattern(regexp = "^(https?://\\S+|/uploads/news/[a-f0-9\\-]{36}\\.(jpg|jpeg|png|webp|avif|gif))$",
            message = "{validation.imageUrl.invalid}")
    private String imageUrl;

    @Size(max = 100, message = "{validation.author.max100}")
    private String author;

    private NewsCategory category = NewsCategory.GENERAL;

    public void setTitle(String title) {
        this.title = TextUtil.cleanLine(title);
    }

    public void setContent(String content) {
        this.content = TextUtil.clean(content);
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = TextUtil.optionalLine(sourceUrl);
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = TextUtil.optionalLine(imageUrl);
    }

    public void setAuthor(String author) {
        this.author = TextUtil.optionalLine(author);
    }
}
