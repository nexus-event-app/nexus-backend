package com.nexus.backend.service;

import com.nexus.backend.dto.request.ResetPasswordRequest;
import com.nexus.backend.entity.User;
import com.nexus.backend.security.UserDetail;

/** Verificación de email y recuperación de contraseña mediante enlaces de un solo uso. */
public interface AccountService {

    void sendEmailVerification(User user);

    /** Siempre responde igual, exista o no la cuenta, para no revelar qué emails están registrados. */
    void requestPasswordReset(String email);

    void resetPassword(ResetPasswordRequest request);

    void verifyEmail(String token);

    void resendEmailVerification(UserDetail actor);

    /** Lanza EmailNotVerifiedException si la configuración exige email verificado para publicar. */
    void assertCanParticipate(UserDetail actor);
}
