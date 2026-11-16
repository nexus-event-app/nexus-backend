package com.nexus.backend.dto.request;

import com.nexus.backend.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dar o quitar el permiso de administración desde el panel. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleRequest {

    @NotNull(message = "{validation.role.required}")
    private User.Role role;
}
