package com.nexus.backend.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateDiscussionRequest extends CreateDiscussionRequest {

    private Boolean isActive;
}
