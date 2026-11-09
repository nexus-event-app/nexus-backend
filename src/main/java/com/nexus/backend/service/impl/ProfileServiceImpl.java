package com.nexus.backend.service.impl;

import com.nexus.backend.dto.request.UpdateProfileRequest;
import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.dto.response.ProfileResponse;
import com.nexus.backend.dto.response.TestimonialResponse;
import com.nexus.backend.entity.User;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.exception.UnauthorizedException;
import com.nexus.backend.mapper.TestimonialMapper;
import com.nexus.backend.repository.DiscussionRepository;
import com.nexus.backend.repository.TestimonialRepository;
import com.nexus.backend.repository.UserRepository;
import com.nexus.backend.repository.spec.DiscussionSpecifications;
import com.nexus.backend.security.UserDetail;
import com.nexus.backend.service.AccountService;
import com.nexus.backend.service.AvatarStorage;
import com.nexus.backend.service.DiscussionService;
import com.nexus.backend.service.ProfileService;
import com.nexus.backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private static final String SEPARATOR = ",";

    private final UserRepository userRepository;
    private final DiscussionRepository discussionRepository;
    private final TestimonialRepository testimonialRepository;
    private final DiscussionService discussionService;
    private final AccountService accountService;
    private final AvatarStorage avatarStorage;
    private final TestimonialMapper testimonialMapper;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getPublicProfile(Long userId) {
        return toResponse(findVisible(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DiscussionResponse> getDiscussions(Long userId, int page, int size, UserDetail viewer) {
        findVisible(userId);
        return discussionService.getDiscussionsByAuthor(userId, page, size, viewer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestimonialResponse> getTestimonials(Long userId) {
        findVisible(userId);
        return testimonialRepository.findPublishedByUserId(userId).stream().map(testimonialMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getOwnProfile(UserDetail actor) {
        return toResponse(loadUser(actor));
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(UpdateProfileRequest request, UserDetail actor) {
        accountService.assertCanParticipate(actor);
        User user = loadUser(actor);
        user.setName(request.getName());
        user.setHeadline(request.getHeadline());
        user.setBio(request.getBio());
        user.setTechnologies(request.getTechnologies().isEmpty() ? null : String.join(SEPARATOR, request.getTechnologies()));
        user.setGithubUrl(request.getGithubUrl());
        user.setLinkedinUrl(request.getLinkedinUrl());
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public ProfileResponse updateAvatar(MultipartFile file, UserDetail actor) {
        accountService.assertCanParticipate(actor);
        User user = loadUser(actor);
        String previous = user.getAvatarUrl();
        String stored = avatarStorage.store(file);
        user.setAvatarUrl(stored);
        ProfileResponse response = toResponse(userRepository.save(user));
        // La foto anterior se borra solo si el cambio llega a guardarse; si falla, se borra la nueva.
        afterTransaction(() -> avatarStorage.delete(previous), () -> avatarStorage.delete(stored));
        return response;
    }

    @Override
    @Transactional
    public ProfileResponse removeAvatar(UserDetail actor) {
        User user = loadUser(actor);
        String previous = user.getAvatarUrl();
        user.setAvatarUrl(null);
        ProfileResponse response = toResponse(userRepository.save(user));
        afterTransaction(() -> avatarStorage.delete(previous), () -> { });
        return response;
    }

    /** Las cuentas desactivadas no tienen perfil público. */
    private User findVisible(Long userId) {
        return userRepository.findById(userId)
                .filter(User::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.notFound"));
    }

    private User loadUser(UserDetail actor) {
        return userRepository.findById(actor.getId())
                .orElseThrow(() -> new UnauthorizedException("error.user.notFound"));
    }

    private ProfileResponse toResponse(User user) {
        List<String> technologies = user.getTechnologies() == null
                ? List.of()
                : Arrays.stream(user.getTechnologies().split(SEPARATOR)).map(String::trim).filter(s -> !s.isEmpty()).toList();
        return new ProfileResponse(
                user.getId(),
                SlugUtil.slugify(user.getName()),
                user.getName(),
                user.getHeadline(),
                user.getBio(),
                technologies,
                user.getGithubUrl(),
                user.getLinkedinUrl(),
                user.getAvatarUrl(),
                user.getCreatedAt(),
                discussionRepository.count(DiscussionSpecifications.isActive()
                        .and(DiscussionSpecifications.hasAuthor(user.getId()))),
                testimonialRepository.countPublishedByUserId(user.getId()));
    }

    private static void afterTransaction(Runnable onCommit, Runnable onRollback) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            onCommit.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    onCommit.run();
                } else {
                    onRollback.run();
                }
            }
        });
    }
}
