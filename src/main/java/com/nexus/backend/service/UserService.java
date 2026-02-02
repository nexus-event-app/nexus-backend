package com.nexus.backend.service;

import com.nexus.backend.dto.request.LoginRequest;
import com.nexus.backend.dto.request.RegisterRequest;
import com.nexus.backend.dto.response.UserResponse;

public interface UserService {
    

    UserResponse register(RegisterRequest request);
    

    UserResponse login(LoginRequest request);
    

    UserResponse findByEmail(String email);
}