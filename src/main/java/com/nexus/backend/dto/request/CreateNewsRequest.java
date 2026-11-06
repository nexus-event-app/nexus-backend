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

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar 200 caracteres")
    private String title;

    @NotBlank(message = "El contenido es obligatorio")
    @Size(max = 20000, message = "El contenido no puede superar 20000 caracteres")
    private String content;

    @Size(max = 500, message = "La URL de la fuente no puede superar 500 caracteres")
    @Pattern(regexp = "^https?://\\S+$", message = "La URL de la fuente debe empezar por http:// o https://")
    private String sourceUrl;

    @Size(max = 500, message = "La URL de la imagen no puede superar 500 caracteres")
    @Pattern(regexp = "^(https?://\\S+|/uploads/news/[a-f0-9\\-]{36}\\.(jpg|jpeg|png|webp|avif|gif))$",
            message = "La URL de la imagen no es válida")
    private String imageUrl;

    @Size(max = 100, message = "El autor no puede superar 100 caracteres")
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
