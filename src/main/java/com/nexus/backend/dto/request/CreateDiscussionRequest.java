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

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres")
    private String title;

    @NotBlank(message = "El contenido es obligatorio")
    @Size(min = 10, max = 10000, message = "El contenido debe tener entre 10 y 10000 caracteres")
    private String content;

    @NotNull(message = "La categoría es obligatoria")
    private DiscussionCategory category;

    public void setTitle(String title) {
        this.title = TextUtil.cleanLine(title);
    }

    public void setContent(String content) {
        this.content = TextUtil.clean(content);
    }
}
