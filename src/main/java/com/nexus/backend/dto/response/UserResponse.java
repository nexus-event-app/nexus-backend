package com.nexus.backend.dto.response;

import com.nexus.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private User.Role role;
    private boolean enabled;
    private boolean emailVerified;
    private String avatarUrl;
    /** Si la cuenta tiene contraseña; las creadas con Google pueden no tenerla. */
    private boolean hasPassword;
    /** La eliminó su titular: el panel la muestra como tal y no deja reactivarla. */
    private boolean deleted;
    private LocalDateTime createdAt;
}
