package com.nexus.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "{validation.link.invalid}")
    @Size(max = 200, message = "{validation.link.invalid}")
    @ToString.Exclude
    private String token;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 72, message = "{validation.password.size}")
    @Pattern(regexp = "^(?=.*\\p{L})(?=.*\\d).*$", message = "{validation.password.pattern}")
    @ToString.Exclude
    private String password;
}
