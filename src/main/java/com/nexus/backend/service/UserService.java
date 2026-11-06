package com.nexus.backend.service;

import com.nexus.backend.dto.request.LoginRequest;
import com.nexus.backend.dto.request.RegisterRequest;
import com.nexus.backend.dto.response.AuthResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.dto.response.UserResponse;
import com.nexus.backend.security.UserDetail;

public interface UserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser(UserDetail principal);

    PageResponse<UserResponse> searchUsers(String text, int page, int size);

    UserResponse updateStatus(Long userId, boolean enabled, UserDetail actor);
}
