package com.nexus.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTestimonialRequest {

    private Boolean isActive;
    private Boolean isPending;
    private Boolean isHighlighted;
}
