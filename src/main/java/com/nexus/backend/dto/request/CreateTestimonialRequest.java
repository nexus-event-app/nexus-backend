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

    @NotBlank(message = "{validation.name.required}")
    @Size(min = 2, max = 100, message = "{validation.name.size.2.100}")
    private String name;

    @Size(max = 100, message = "{validation.role.max100}")
    private String role;

    @Size(max = 100, message = "{validation.location.max100}")
    private String location;

    @NotBlank(message = "{validation.testimonial.required}")
    @Size(min = 50, max = 300, message = "{validation.testimonial.size}")
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
