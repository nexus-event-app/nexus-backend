package com.nexus.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Código de un solo uso que la web recibe al volver de Google (para iniciar sesión o terminar el alta). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleTokenRequest {

    @NotBlank(message = "{validation.link.invalid}")
    @Size(max = 200, message = "{validation.link.invalid}")
    @ToString.Exclude
    private String token;
}
