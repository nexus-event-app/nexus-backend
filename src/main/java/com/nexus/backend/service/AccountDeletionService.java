package com.nexus.backend.service;

import com.nexus.backend.dto.request.DeleteAccountRequest;
import com.nexus.backend.security.UserDetail;

public interface AccountDeletionService {

    void deleteOwnAccount(DeleteAccountRequest request, UserDetail actor);
}
