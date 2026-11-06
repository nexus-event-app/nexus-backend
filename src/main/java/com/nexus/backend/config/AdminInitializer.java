package com.nexus.backend.config;

import com.nexus.backend.entity.User;
import com.nexus.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Crea el primer ADMIN a partir de ADMIN_EMAIL / ADMIN_PASSWORD / ADMIN_NAME si aún no existe. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.name:Administrador}")
    private String adminName;

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD no definidos: no se crea ningún administrador inicial");
            return;
        }
        if (adminPassword.length() < MIN_PASSWORD_LENGTH) {
            log.warn("ADMIN_PASSWORD debe tener al menos {} caracteres: no se crea el administrador", MIN_PASSWORD_LENGTH);
            return;
        }

        String email = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            log.info("El administrador inicial ya existe");
            return;
        }

        User admin = new User();
        admin.setEmail(email);
        admin.setName(adminName.isBlank() ? "Administrador" : adminName.trim());
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(User.Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);
        log.info("Administrador inicial creado");
    }
}
