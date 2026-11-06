package com.nexus.backend.dto.request;

import com.nexus.backend.enums.ImportSourceType;
import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.util.TextUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportSourceRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @NotNull(message = "El tipo de fuente es obligatorio")
    private ImportSourceType type;

    @NotBlank(message = "La URL es obligatoria")
    @Size(max = 500, message = "La URL no puede superar 500 caracteres")
    @Pattern(regexp = "^https?://\\S+$", message = "La URL debe empezar por http:// o https://")
    private String url;

    private NewsCategory newsCategory;

    private Boolean autoPublish = true;

    private Boolean active = true;

    private Boolean remoteOnly = false;

    @Size(max = 500, message = "Las palabras clave no pueden superar 500 caracteres")
    private String keywords;

    /** Si no se indica, se usa el mínimo del tipo de fuente. */
    @Min(value = 30, message = "El intervalo mínimo es de 30 minutos")
    @Max(value = 10080, message = "El intervalo máximo es de una semana (10080 minutos)")
    private Integer intervalMinutes;

    public void setName(String name) {
        this.name = TextUtil.cleanLine(name);
    }

    public void setKeywords(String keywords) {
        this.keywords = TextUtil.optionalLine(keywords);
    }

    public void setUrl(String url) {
        this.url = TextUtil.optionalLine(url);
    }
}
