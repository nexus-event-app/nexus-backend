package com.nexus.backend.dto.request;

import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.WorkMode;
import com.nexus.backend.util.TextUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class CreateJobRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar 200 caracteres")
    private String title;

    @NotBlank(message = "La empresa es obligatoria")
    @Size(max = 120, message = "La empresa no puede superar 120 caracteres")
    private String company;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 20000, message = "La descripción no puede superar 20000 caracteres")
    private String description;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 120, message = "La ubicación no puede superar 120 caracteres")
    private String location;

    @NotNull(message = "La modalidad de trabajo es obligatoria")
    private WorkMode workMode;

    @NotNull(message = "El tipo de contrato es obligatorio")
    private ContractType contractType;

    @NotNull(message = "El nivel es obligatorio")
    private JobLevel level;

    @Size(max = 10, message = "No puede haber más de 10 etiquetas")
    private List<@NotBlank(message = "Las etiquetas no pueden estar vacías")
            @Size(max = 30, message = "Cada etiqueta no puede superar 30 caracteres") String> tags = new ArrayList<>();

    @NotBlank(message = "La URL de solicitud es obligatoria")
    @Size(max = 500, message = "La URL de solicitud no puede superar 500 caracteres")
    @Pattern(regexp = "^https?://\\S+$", message = "La URL de solicitud debe empezar por http:// o https://")
    private String applyUrl;

    @Size(max = 100, message = "La fuente no puede superar 100 caracteres")
    private String source;

    private LocalDateTime expiresAt;

    public void setTitle(String title) {
        this.title = TextUtil.cleanLine(title);
    }

    public void setCompany(String company) {
        this.company = TextUtil.cleanLine(company);
    }

    public void setDescription(String description) {
        this.description = TextUtil.clean(description);
    }

    public void setLocation(String location) {
        this.location = TextUtil.cleanLine(location);
    }

    public void setApplyUrl(String applyUrl) {
        this.applyUrl = TextUtil.cleanLine(applyUrl);
    }

    public void setSource(String source) {
        this.source = TextUtil.optionalLine(source);
    }

    public void setTags(List<String> tags) {
        Set<String> cleaned = new LinkedHashSet<>();
        if (tags != null) {
            for (String tag : tags) {
                String value = tag == null ? null : TextUtil.optionalLine(tag.replace(',', ' '));
                if (value != null) {
                    cleaned.add(value);
                }
            }
        }
        this.tags = new ArrayList<>(cleaned);
    }
}
