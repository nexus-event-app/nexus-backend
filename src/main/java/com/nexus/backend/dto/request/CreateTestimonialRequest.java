package com.nexus.backend.dto.request;

import com.nexus.backend.util.TextUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestimonialRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @Size(max = 100, message = "El rol no puede superar 100 caracteres")
    private String role;

    @Size(max = 100, message = "La ubicación no puede superar 100 caracteres")
    private String location;

    @NotBlank(message = "El testimonio es obligatorio")
    @Size(min = 50, max = 300, message = "El testimonio debe tener entre 50 y 300 caracteres")
    private String content;

    public void setName(String name) {
        this.name = TextUtil.cleanLine(name);
    }

    public void setRole(String role) {
        this.role = TextUtil.optionalLine(role);
    }

    public void setLocation(String location) {
        this.location = TextUtil.optionalLine(location);
    }

    public void setContent(String content) {
        this.content = TextUtil.clean(content);
    }
}
