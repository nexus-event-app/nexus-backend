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

/**
 * Cuentas que entran con Google. Al volver de Google:
 * <ul>
 *   <li>si la cuenta de Google ya está vinculada, se entra;</li>
 *   <li>si existe una cuenta con el mismo email (y Google lo ha verificado), se vincula y se entra;</li>
 *   <li>si es alguien nuevo, no se crea nada todavía: queda pendiente hasta que acepte las condiciones.</li>
 * </ul>
 * La web solo recibe códigos de un solo uso y de vida corta, que se canjean por la sesión con una petición aparte:
 * así el token de sesión nunca viaja en una URL. Los códigos se guardan en memoria (solo su hash): basta con una
 * instancia; si se reparte entre varias, habría que llevarlos a la base de datos.
 */
@Slf4j
@Service
public class GoogleAccountService {

    static final String REJECTED_DISABLED = "disabled";
    static final String REJECTED_UNVERIFIED = "unverified";
    private static final int NAME_MIN = 2;
    private static final int NAME_MAX = 50;

    private final UserRepository userRepository;
    private final UserService userService;

    /** Hash del código → id de la cuenta. Dos minutos dan de sobra para que la web lo canjee al cargar. */
    private final Cache<String, Long> loginCodes = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(2)).maximumSize(10_000).build();

    /** Hash del código → datos de Google, mientras la persona lee y acepta las condiciones. */
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
            // Sin email verificado no se puede saber si la cuenta es de verdad de esa persona: ni vincular ni crear.
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

    /** Canjea el código de un solo uso por la sesión. */
    @Transactional(readOnly = true)
    public AuthResponse exchange(String code) {
        Long userId = loginCodes.asMap().remove(OneTimeTokens.hash(code));
        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        if (user == null || !user.isEnabled()) {
            throw new UnauthorizedException("error.google.expired");
        }
        return userService.buildAuthResponse(user);
    }

    /** Nombre y email que se muestran en «Ya casi está». No consume el código. */
    public GoogleSignupResponse pendingSignup(String token) {
        GoogleProfile profile = pendingSignups.getIfPresent(OneTimeTokens.hash(token));
        if (profile == null) {
            throw new UnauthorizedException("error.google.expired");
        }
        return new GoogleSignupResponse(nameFor(profile), normalizeEmail(profile.email()));
    }

    /** Crea la cuenta tras aceptar las condiciones (la validación de la petición ya exige la casilla marcada). */
    @Transactional
    public AuthResponse completeSignup(String token) {
        GoogleProfile profile = pendingSignups.asMap().remove(OneTimeTokens.hash(token));
        if (profile == null) {
            throw new UnauthorizedException("error.google.expired");
        }
        String email = normalizeEmail(profile.email());
        // Por si mientras tanto se registró con ese email (o se vinculó en otra pestaña): se entra en esa cuenta.
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
            // Google ya ha comprobado el email: no hace falta mandar el enlace de verificación.
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
        // Google confirma que el email es suyo: cuenta como verificado.
        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(LocalDateTime.now());
        }
        userRepository.save(user);
    }

    /** El nombre de Google, limpio y dentro de los límites del registro; si no sirve, la parte del email antes de @. */
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
