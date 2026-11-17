package com.nexus.backend.service;

import com.nexus.backend.dto.request.DeleteAccountRequest;
import com.nexus.backend.security.UserDetail;

public interface AccountDeletionService {

    /**
     * Elimina la cuenta de quien la pide: borra sus datos personales y lo que solo era suyo, y deja su participación
     * en el foro firmada como «Cuenta eliminada».
     */
    void deleteOwnAccount(DeleteAccountRequest request, UserDetail actor);
}
