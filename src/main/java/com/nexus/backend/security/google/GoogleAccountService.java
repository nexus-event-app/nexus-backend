package com.nexus.backend.security.google;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nexus.backend.dto.response.AuthResponse;
import com.nexus.backend.dto.response.GoogleSignupResponse;
import com.nexus.backend.entity.User;
import com.nexus.backend.exception.UnauthorizedException;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.security.OneTimeTokens;
import com.nexus.backend.service.UserService;
import com.nexus.backend.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Slf4j
@Service
public class GoogleAccountService {

    static final String REJECTED_DISABLED = "disabled";
    static final String REJECTED_UNVERIFIED = "unverified";
    private static final int NAME_MIN = 2;
    private static final int NAME_MAX = 50;

    private final UserRepository userRepository;
    private final UserService userService;

    private final Cache<String, Long> loginCodes = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(2)).maximumSize(10_000).build();

    private final Cache<String, GoogleProfile> pendingSignups = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15)).maximumSize(10_000).build();

    public GoogleAccountService(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional
    public GoogleLoginOutcome handleLogin(GoogleProfile profile) {
        User user = userRepository.findByGoogleSubject(profile.subject()).orElse(null);
        if (user == null) {
            String email = normalizeEmail(profile.email());
            if (email == null || !profile.emailVerified()) {
                return new GoogleLoginOutcome.Rejected(REJECTED_UNVERIFIED);
            }
            user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                String token = OneTimeTokens.generate();
                pendingSignups.put(OneTimeTokens.hash(token), profile);
                return new GoogleLoginOutcome.SignupPending(token);
            }
            link(user, profile);
        }
        if (!user.isEnabled()) {
            return new GoogleLoginOutcome.Rejected(REJECTED_DISABLED);
        }
        String code = OneTimeTokens.generate();
        loginCodes.put(OneTimeTokens.hash(code), user.getId());
        return new GoogleLoginOutcome.SignedIn(code);
    }

    @Transactional(readOnly = true)
    public AuthResponse exchange(String code) {
        Long userId = loginCodes.asMap().remove(OneTimeTokens.hash(code));
        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        if (user == null || !user.isEnabled()) {
            throw new UnauthorizedException("error.google.expired");
        }
        return userService.buildAuthResponse(user);
    }

    public GoogleSignupResponse pendingSignup(String token) {
        GoogleProfile profile = pendingSignups.getIfPresent(OneTimeTokens.hash(token));
        if (profile == null) {
            throw new UnauthorizedException("error.google.expired");
        }
        return new GoogleSignupResponse(nameFor(profile), normalizeEmail(profile.email()));
    }

    @Transactional
    public AuthResponse completeSignup(String token) {
        GoogleProfile profile = pendingSignups.asMap().remove(OneTimeTokens.hash(token));
        if (profile == null) {
            throw new UnauthorizedException("error.google.expired");
        }
        String email = normalizeEmail(profile.email());
        User user = userRepository.findByGoogleSubject(profile.subject())
                .or(() -> userRepository.findByEmail(email))
                .orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(nameFor(profile));
            user.setPassword(null);
            user.setRole(User.Role.USER);
            user.setEnabled(true);
            user.setGoogleSubject(profile.subject());
            user.setEmailVerifiedAt(LocalDateTime.now());
            user.setTermsAcceptedAt(LocalDateTime.now());
            user = userRepository.save(user);
            log.info("Usuario registrado con Google: id={}", user.getId());
        } else {
            link(user, profile);
        }
        if (!user.isEnabled()) {
            throw new UnauthorizedException("error.account.disabled");
        }
        return userService.buildAuthResponse(user);
    }

    private void link(User user, GoogleProfile profile) {
        if (user.getGoogleSubject() == null) {
            user.setGoogleSubject(profile.subject());
            log.info("Cuenta id={} vinculada con Google", user.getId());
        }
        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(LocalDateTime.now());
        }
        userRepository.save(user);
    }

    static String nameFor(GoogleProfile profile) {
        String name = TextUtil.truncate(TextUtil.cleanLine(profile.name()), NAME_MAX);
        if (name == null || name.length() < NAME_MIN) {
            String email = normalizeEmail(profile.email());
            String local = email == null ? "" : email.substring(0, email.indexOf('@'));
            name = TextUtil.truncate(local, NAME_MAX);
        }
        return name;
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
