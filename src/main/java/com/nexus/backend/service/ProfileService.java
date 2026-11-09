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

    PageResponse<DiscussionResponse> getDiscussions(Long userId, int page, int size, UserDetail viewer);

    List<TestimonialResponse> getTestimonials(Long userId);

    ProfileResponse getOwnProfile(UserDetail actor);

    ProfileResponse updateProfile(UpdateProfileRequest request, UserDetail actor);

    ProfileResponse updateAvatar(MultipartFile file, UserDetail actor);

    ProfileResponse removeAvatar(UserDetail actor);
}
