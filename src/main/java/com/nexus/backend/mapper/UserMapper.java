package com.nexus.backend.mapper;

import com.nexus.backend.dto.request.RegisterRequest;
import com.nexus.backend.dto.response.UserResponse;
import com.nexus.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public User toEntity(RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); 
        user.setRole(User.Role.ADMIN);
        return user;
    }
    
    public UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            null,
            user.getCreatedAt()
        );
    }
}