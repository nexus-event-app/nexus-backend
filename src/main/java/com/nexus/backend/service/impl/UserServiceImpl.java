package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.LoginRequest;
import com.nexus.backend.dto.request.RegisterRequest;
import com.nexus.backend.dto.response.AuthResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.dto.response.UserResponse;
import com.nexus.backend.entity.User;
import com.nexus.backend.exception.BadRequestException;
import com.nexus.backend.exception.ConflictException;
import com.nexus.backend.exception.ForbiddenOperationException;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.exception.UnauthorizedException;
import com.nexus.backend.mapper.UserMapper;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.repository.spec.UserSpecifications;
import com.nexus.backend.security.JwtUtil;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.UserService;
import com.nexus.backend.util.Pagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new BadRequestException("La contraseña no puede superar los 72 bytes");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Ya existe una cuenta con ese email");
        }

        User user = userMapper.toEntity(request, passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);
        log.info("Usuario registrado: id={}", saved.getId());
        return buildAuthResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }
        if (!user.isEnabled()) {
            throw new UnauthorizedException("Tu cuenta está desactivada. Contacta con un administrador");
        }
        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserDetail principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchUsers(String text, int page, int size) {
        Page<User> users = userRepository.findAll(UserSpecifications.matches(text), Pagination.newestFirst(page, size));
        return PageResponse.from(users.map(userMapper::toResponse));
    }

    @Override
    @Transactional
    public UserResponse updateStatus(Long userId, boolean enabled, UserDetail actor) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!enabled) {
            if (target.getId().equals(actor.getId())) {
                throw new ForbiddenOperationException("No puedes desactivar tu propia cuenta");
            }
            if (target.getRole() == User.Role.ADMIN) {
                throw new ForbiddenOperationException("No se puede desactivar a otro administrador");
            }
        }
        target.setEnabled(enabled);
        log.info("Usuario id={} {} por admin id={}", target.getId(), enabled ? "activado" : "desactivado", actor.getId());
        return userMapper.toResponse(userRepository.save(target));
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return AuthResponse.builder()
                .token(token)
                .user(userMapper.toResponse(user))
                .build();
    }
}
