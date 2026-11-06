package com.nexus.backend.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateJobRequest extends CreateJobRequest {

    private Boolean isActive;
}
