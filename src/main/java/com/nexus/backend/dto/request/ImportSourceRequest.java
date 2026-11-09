package com.nexus.backend.dto.request;

import com.nexus.backend.enums.ImportSourceType;
import com.nexus.backend.enums.NewsCategory;
import com.nexus.backend.util.TextUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportSourceRequest {

    @NotBlank(message = "{validation.name.required}")
    @Size(min = 2, max = 100, message = "{validation.name.size.2.100}")
    private String name;

    @NotNull(message = "{validation.source.type.required}")
    private ImportSourceType type;

    @NotBlank(message = "{validation.url.required}")
    @Size(max = 500, message = "{validation.url.max500}")
    @Pattern(regexp = "^https?://\\S+$", message = "{validation.url.pattern}")
    private String url;

    private NewsCategory newsCategory;

    private Boolean autoPublish = true;

    private Boolean active = true;

    private Boolean remoteOnly = false;

    private Boolean useSourceImages = true;

    @Size(max = 500, message = "{validation.keywords.max500}")
    private String keywords;

    @Size(max = 500, message = "{validation.excludeKeywords.max500}")
    private String excludeKeywords;

    /** Si no se indica, se usa el mínimo del tipo de fuente. */
    @Min(value = 30, message = "{validation.interval.min}")
    @Max(value = 10080, message = "{validation.interval.max}")
    private Integer intervalMinutes;

    public void setName(String name) {
        this.name = TextUtil.cleanLine(name);
    }

    public void setKeywords(String keywords) {
        this.keywords = TextUtil.optionalLine(keywords);
    }

    public void setExcludeKeywords(String excludeKeywords) {
        this.excludeKeywords = TextUtil.optionalLine(excludeKeywords);
    }

    public void setUrl(String url) {
        this.url = TextUtil.optionalLine(url);
    }
}
