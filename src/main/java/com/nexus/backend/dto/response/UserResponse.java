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
    private LocalDateTime createdAt;
}
