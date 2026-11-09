package com.nexus.backend.dto.response;

import com.nexus.backend.enums.ImportSourceType;
import com.nexus.backend.enums.ImportStatus;
import com.nexus.backend.enums.NewsCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportSourceResponse {

    private Long id;
    private String name;
    private ImportSourceType type;
    private String url;
    private NewsCategory newsCategory;
    private Boolean autoPublish;
    private Boolean active;
    private Boolean remoteOnly;
    private Boolean useSourceImages;
    private String keywords;
    private String excludeKeywords;
    private Integer intervalMinutes;
    private Integer minIntervalMinutes;
    private LocalDateTime lastRunAt;
    private ImportStatus lastStatus;
    private String lastMessage;
    private Integer lastImportedCount;
    private Boolean running;
    private LocalDateTime createdAt;
}
