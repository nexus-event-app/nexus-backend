package com.nexus.backend.dto.response;

import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.WorkMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    private Long id;
    private String slug;
    private String title;
    private String company;
    private String description;
    private String location;
    private WorkMode workMode;
    private ContractType contractType;
    private JobLevel level;
    private List<String> tags;
    private String applyUrl;
    private String source;
    private Boolean isActive;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
