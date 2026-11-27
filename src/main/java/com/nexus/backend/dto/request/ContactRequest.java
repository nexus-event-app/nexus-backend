package com.nexus.backend.dto.request;

import com.nexus.backend.enums.ContactReason;
import com.nexus.backend.util.TextUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContactRequest {

    @NotBlank(message = "{validation.name.required}")
    @Size(min = 2, max = 80, message = "{validation.contact.name.size}")
    private String name;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    @Size(max = 100, message = "{validation.email.max100}")
    private String email;

    @NotNull(message = "{validation.contact.reason.required}")
    private ContactReason reason;

    @NotBlank(message = "{validation.contact.message.required}")
    @Size(min = 10, max = 3000, message = "{validation.contact.message.size}")
    private String message;

    private String website;

    public void setName(String name) {
        this.name = TextUtil.cleanLine(name);
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public void setMessage(String message) {
        this.message = TextUtil.clean(message);
    }
}
