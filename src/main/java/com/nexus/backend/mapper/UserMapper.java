package com.nexus.backend.mapper;

import com.nexus.backend.dto.request.RegisterRequest;
import com.nexus.backend.dto.response.UserResponse;
import com.nexus.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /** El registro público siempre crea usuarios con rol USER. */
    public User toEntity(RegisterRequest request, String encodedPassword) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        return user;
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
