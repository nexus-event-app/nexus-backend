package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.DeleteAccountRequest;
import com.nexus.backend.entity.User;
import com.nexus.backend.exception.BadRequestException;
import com.nexus.backend.exception.ForbiddenOperationException;
import com.nexus.backend.repository.LearningProgressRepository;
import com.nexus.backend.repository.NotificationRepository;
import com.nexus.backend.repository.SavedJobRepository;
import com.nexus.backend.repository.TestimonialRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.repository.UserTokenRepository;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.AccountDeletionService;
import com.nexus.backend.service.AvatarStorage;
import com.nexus.backend.service.TestimonialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountDeletionServiceImpl implements AccountDeletionService {

    static final String DELETED_NAME = "Cuenta eliminada";

    private final UserRepository userRepository;
    private final TestimonialRepository testimonialRepository;
    private final TestimonialService testimonialService;
    private final SavedJobRepository savedJobRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final NotificationRepository notificationRepository;
    private final UserTokenRepository userTokenRepository;
    private final AvatarStorage avatarStorage;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void deleteOwnAccount(DeleteAccountRequest request, UserDetail actor) {
        User user = userRepository.getSessionUser(actor.getId());
        if (user.getRole() == User.Role.ADMIN) {
            throw new ForbiddenOperationException("error.account.adminCannotDelete");
        }
        if (user.getPassword() != null
                && (request.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword()))) {
            throw new BadRequestException("error.account.wrongPassword");
        }

        Long id = user.getId();
        testimonialRepository.findIdsByUserId(id).forEach(testimonialService::deleteTestimonial);
        savedJobRepository.deleteAllByUserId(id);
        learningProgressRepository.deleteAllByUserId(id);
        notificationRepository.deleteAllInvolving(id);
        userTokenRepository.deleteAllByUserId(id);

        String avatar = user.getAvatarUrl();
        anonymize(user);
        userRepository.save(user);
        afterCommit(() -> avatarStorage.delete(avatar));
        log.info("Cuenta eliminada por su titular: usuario id={}", id);
    }

    private static void anonymize(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setName(DELETED_NAME);
        user.setEmail("eliminada-" + user.getId() + "@cuenta-eliminada.invalid");
        user.setPassword(null);
        user.setGoogleSubject(null);
        user.setHeadline(null);
        user.setBio(null);
        user.setTechnologies(null);
        user.setGithubUrl(null);
        user.setLinkedinUrl(null);
        user.setAvatarUrl(null);
        user.setEmailVerifiedAt(null);
        user.setTermsAcceptedAt(null);
        user.setEnabled(false);
        user.setDeletedAt(now);
        user.setCredentialsChangedAt(now);
    }

    private static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
