package com.nexus.backend.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** «Ya casi está»: terminar el alta con Google aceptando las condiciones y la política de privacidad. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleSignupRequest {

    @NotBlank(message = "{validation.link.invalid}")
    @Size(max = 200, message = "{validation.link.invalid}")
    @ToString.Exclude
    private String token;

    @NotNull(message = "{validation.terms.required}")
    @AssertTrue(message = "{validation.terms.required}")
    private Boolean acceptTerms;
}
