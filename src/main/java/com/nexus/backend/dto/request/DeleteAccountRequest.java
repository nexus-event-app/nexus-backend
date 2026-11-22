package com.nexus.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountRequest {

    @Size(max = 72, message = "{validation.password.max72}")
    @ToString.Exclude
    private String password;
}
