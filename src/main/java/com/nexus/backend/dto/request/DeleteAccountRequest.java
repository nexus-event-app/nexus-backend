package com.nexus.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** Confirmación para eliminar la propia cuenta: la contraseña actual, si la cuenta tiene (las de Google pueden no). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountRequest {

    @Size(max = 72, message = "{validation.password.max72}")
    @ToString.Exclude
    private String password;
}
