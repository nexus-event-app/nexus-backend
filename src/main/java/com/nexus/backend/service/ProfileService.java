package com.nexus.backend.service;

import com.nexus.backend.dto.request.UpdateProfileRequest;
import com.nexus.backend.dto.response.PageResponse;
import com.nexus.backend.dto.response.ProfileResponse;
import com.nexus.backend.dto.response.DiscussionResponse;
import com.nexus.backend.dto.response.TestimonialResponse;
import com.nexus.backend.security.UserDetail;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfileService {

    ProfileResponse getPublicProfile(Long userId);

    /** Actividad de la persona con sesión iniciada: solo la ve ella. */
    PageResponse<DiscussionResponse> getOwnDiscussions(UserDetail actor, int page, int size);

    List<TestimonialResponse> getOwnTestimonials(UserDetail actor);

    ProfileResponse getOwnProfile(UserDetail actor);

    ProfileResponse updateProfile(UpdateProfileRequest request, UserDetail actor);

    ProfileResponse updateAvatar(MultipartFile file, UserDetail actor);

    ProfileResponse removeAvatar(UserDetail actor);
}
