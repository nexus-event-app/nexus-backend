package com.nexus.backend.dto.request;

import com.nexus.backend.enums.ContractType;
import com.nexus.backend.enums.JobLevel;
import com.nexus.backend.enums.WorkMode;
import com.nexus.backend.util.TextUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class CreateJobRequest {

    @NotBlank(message = "{validation.title.required}")
    @Size(max = 200, message = "{validation.title.max200}")
    private String title;

    @NotBlank(message = "{validation.company.required}")
    @Size(max = 120, message = "{validation.company.max120}")
    private String company;

    @NotBlank(message = "{validation.description.required}")
    @Size(max = 20000, message = "{validation.description.max20000}")
    private String description;

    @NotBlank(message = "{validation.location.required}")
    @Size(max = 120, message = "{validation.location.max120}")
    private String location;

    @NotNull(message = "{validation.workMode.required}")
    private WorkMode workMode;

    @NotNull(message = "{validation.contractType.required}")
    private ContractType contractType;

    @NotNull(message = "{validation.level.required}")
    private JobLevel level;

    @Size(max = 10, message = "{validation.tags.max10}")
    private List<@NotBlank(message = "{validation.tag.blank}")
            @Size(max = 30, message = "{validation.tag.max30}") String> tags = new ArrayList<>();

    @NotBlank(message = "{validation.applyUrl.required}")
    @Size(max = 500, message = "{validation.applyUrl.max500}")
    @Pattern(regexp = "^https?://\\S+$", message = "{validation.applyUrl.pattern}")
    private String applyUrl;

    @Size(max = 100, message = "{validation.source.max100}")
    private String source;

    private LocalDateTime expiresAt;

    public void setTitle(String title) {
        this.title = TextUtil.cleanLine(title);
    }

    public void setCompany(String company) {
        this.company = TextUtil.cleanLine(company);
    }

    public void setDescription(String description) {
        this.description = TextUtil.clean(description);
    }

    public void setLocation(String location) {
        this.location = TextUtil.cleanLine(location);
    }

    public void setApplyUrl(String applyUrl) {
        this.applyUrl = TextUtil.cleanLine(applyUrl);
    }

    public void setSource(String source) {
        this.source = TextUtil.optionalLine(source);
    }

    public void setTags(List<String> tags) {
        Set<String> cleaned = new LinkedHashSet<>();
        if (tags != null) {
            for (String tag : tags) {
                String value = tag == null ? null : TextUtil.optionalLine(tag.replace(',', ' '));
                if (value != null) {
                    cleaned.add(value);
                }
            }
        }
        this.tags = new ArrayList<>(cleaned);
    }
}
