package com.nexus.backend.dto.request;

import com.nexus.backend.util.TextUtil;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Locale;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "{validation.name.required}")
    @Size(min = 2, max = 50, message = "{validation.name.size.2.50}")
    private String name;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    @Size(max = 100, message = "{validation.email.max100}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 72, message = "{validation.password.size}")
    @Pattern(regexp = "^(?=.*\\p{L})(?=.*\\d).*$", message = "{validation.password.pattern}")
    @ToString.Exclude
    private String password;

    /** Aceptación obligatoria del aviso legal, las condiciones de uso y la política de privacidad. */
    @NotNull(message = "{validation.terms.required}")
    @AssertTrue(message = "{validation.terms.required}")
    private Boolean acceptTerms;

    public void setName(String name) {
        this.name = TextUtil.cleanLine(name);
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
