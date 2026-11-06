package com.nexus.backend.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateNewsRequest extends CreateNewsRequest {

    private Boolean isActive;
}
