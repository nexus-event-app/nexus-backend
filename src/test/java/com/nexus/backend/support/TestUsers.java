package com.nexus.backend.support;

import com.nexus.backend.entity.User;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TestUsers {

    public static final String PASSWORD = "Clave12345";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private String encodedPassword;

    public User create(String name, User.Role role, boolean verified) {
        User user = new User();
        user.setName(name);
        user.setEmail("test-" + UUID.randomUUID() + "@test.com");
        user.setPassword(encodedPassword());
        user.setRole(role);
        user.setEnabled(true);
        user.setEmailVerifiedAt(verified ? LocalDateTime.now() : null);
        return userRepository.save(user);
    }

    public String bearer(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
    }

    private String encodedPassword() {
        if (encodedPassword == null) {
            encodedPassword = passwordEncoder.encode(PASSWORD);
        }
        return encodedPassword;
    }
}
