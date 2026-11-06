package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.ResetPasswordRequest;
import com.nexus.backend.entity.User;
import com.nexus.backend.entity.UserToken;
import com.nexus.backend.enums.UserTokenType;
import com.nexus.backend.exception.BadRequestException;
import com.nexus.backend.exception.EmailNotVerifiedException;
import com.nexus.backend.exception.UnauthorizedException;
import com.nexus.backend.mail.EmailRequestedEvent;
import com.nexus.backend.mail.EmailTemplates;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.repository.UserTokenRepository;
import com.nexus.backend.security.OneTimeTokens;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    private static final int BCRYPT_MAX_BYTES = 72;
    private static final String INVALID_LINK = "El enlace no es válido o ha caducado. Pide uno nuevo";

    private final UserRepository userRepository;
    private final UserTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailTemplates emailTemplates;
    private final ApplicationEventPublisher events;
    private final boolean requireVerifiedEmail;

    public AccountServiceImpl(UserRepository userRepository,
                              UserTokenRepository tokenRepository,
                              PasswordEncoder passwordEncoder,
                              EmailTemplates emailTemplates,
                              ApplicationEventPublisher events,
                              @Value("${app.auth.require-verified-email:true}") boolean requireVerifiedEmail) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailTemplates = emailTemplates;
        this.events = events;
        this.requireVerifiedEmail = requireVerifiedEmail;
    }

    @Override
    @Transactional
    public void sendEmailVerification(User user) {
        String token = issueToken(user, UserTokenType.EMAIL_VERIFICATION);
        events.publishEvent(new EmailRequestedEvent(emailTemplates.emailVerification(user.getEmail(), user.getName(), token)));
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email)
                .filter(User::isEnabled)
                .ifPresentOrElse(user -> {
                    String token = issueToken(user, UserTokenType.PASSWORD_RESET);
                    events.publishEvent(new EmailRequestedEvent(emailTemplates.passwordReset(user.getEmail(), user.getName(), token)));
                    log.info("Enlace de recuperación de contraseña emitido para el usuario id={}", user.getId());
                }, () -> log.info("Recuperación de contraseña pedida para un email sin cuenta activa"));
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new BadRequestException("La contraseña no puede superar los 72 bytes");
        }
        UserToken token = consume(request.getToken(), UserTokenType.PASSWORD_RESET);
        User user = token.getUser();
        if (!user.isEnabled()) {
            throw new BadRequestException(INVALID_LINK);
        }
        LocalDateTime now = LocalDateTime.now();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCredentialsChangedAt(now);
        // Quien recibe el enlace en su bandeja demuestra que el email es suyo.
        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(now);
        }
        log.info("Contraseña restablecida para el usuario id={}", user.getId());
    }

    @Override
    @Transactional
    public void verifyEmail(String rawToken) {
        UserToken token = consume(rawToken, UserTokenType.EMAIL_VERIFICATION);
        User user = token.getUser();
        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(LocalDateTime.now());
            log.info("Email verificado para el usuario id={}", user.getId());
        }
    }

    @Override
    @Transactional
    public void resendEmailVerification(UserDetail actor) {
        User user = userRepository.findById(actor.getId())
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));
        if (user.isEmailVerified()) {
            throw new BadRequestException("Tu email ya está verificado");
        }
        sendEmailVerification(user);
    }

    @Override
    public void assertCanParticipate(UserDetail actor) {
        if (requireVerifiedEmail && !actor.getUser().isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }
    }

    /** Limpia cada día los tokens caducados hace más de una semana. */
    @Scheduled(cron = "${app.auth.token-cleanup-cron:0 30 4 * * *}")
    @Transactional
    public void purgeExpiredTokens() {
        int removed = tokenRepository.deleteExpiredBefore(LocalDateTime.now().minusDays(7));
        if (removed > 0) {
            log.info("Borrados {} tokens de cuenta caducados", removed);
        }
    }

    /** Invalida los tokens pendientes del mismo tipo y emite uno nuevo; devuelve el token en claro. */
    private String issueToken(User user, UserTokenType type) {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.invalidatePending(user.getId(), type, now);
        String raw = OneTimeTokens.generate();
        UserToken token = new UserToken();
        token.setUser(user);
        token.setType(type);
        token.setTokenHash(OneTimeTokens.hash(raw));
        token.setExpiresAt(now.plus(type.getValidity()));
        tokenRepository.save(token);
        return raw;
    }

    private UserToken consume(String rawToken, UserTokenType type) {
        LocalDateTime now = LocalDateTime.now();
        UserToken token = tokenRepository.findByHashAndType(OneTimeTokens.hash(rawToken.trim()), type)
                .filter(candidate -> candidate.isUsable(now))
                .orElseThrow(() -> new BadRequestException(INVALID_LINK));
        tokenRepository.invalidatePending(token.getUser().getId(), type, now);
        return token;
    }
}
