package com.nexus.backend.service;

import com.nexus.backend.dto.request.ResetPasswordRequest;
import com.nexus.backend.entity.User;
import com.nexus.backend.security.UserDetail;

public interface AccountService {

    void sendEmailVerification(User user);

    void requestPasswordReset(String email);

    void resetPassword(ResetPasswordRequest request);

    void verifyEmail(String token);

    void resendEmailVerification(UserDetail actor);

    void assertCanParticipate(UserDetail actor);
}
