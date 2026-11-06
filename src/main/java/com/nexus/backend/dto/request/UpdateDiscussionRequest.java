package com.nexus.backend.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateDiscussionRequest extends CreateDiscussionRequest {

    /** Solo se tiene en cuenta si quien edita es ADMIN (permite ocultar o restaurar una discusión). */
    private Boolean isActive;
}
