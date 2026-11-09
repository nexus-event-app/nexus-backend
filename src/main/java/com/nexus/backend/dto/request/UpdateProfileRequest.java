package com.nexus.backend.dto.request;

import com.nexus.backend.util.TextUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Datos del perfil público que la propia persona puede editar. Todo salvo el nombre es opcional. */
@Getter
@NoArgsConstructor
public class UpdateProfileRequest {

    /** Perfil de GitHub: https://github.com/usuario (sin repositorios ni otras rutas). */
    public static final String GITHUB_PATTERN = "^https://(www\\.)?github\\.com/[A-Za-z0-9](?:[A-Za-z0-9-]{0,38})/?$";
    /** Perfil personal de LinkedIn: https://www.linkedin.com/in/usuario. */
    public static final String LINKEDIN_PATTERN = "^https://([a-z]{2,3}\\.)?linkedin\\.com/in/[A-Za-z0-9\\-_%.]{3,100}/?$";

    @NotBlank(message = "{validation.name.required}")
    @Size(min = 2, max = 50, message = "{validation.name.size.2.50}")
    private String name;

    @Size(max = 80, message = "{validation.headline.max80}")
    private String headline;

    @Size(max = 300, message = "{validation.bio.max300}")
    private String bio;

    @Size(max = 12, message = "{validation.technologies.max12}")
    private List<@Size(max = 30, message = "{validation.technology.max30}") String> technologies = List.of();

    @Size(max = 200, message = "{validation.link.invalid}")
    @Pattern(regexp = GITHUB_PATTERN, message = "{validation.github.pattern}")
    private String githubUrl;

    @Size(max = 200, message = "{validation.link.invalid}")
    @Pattern(regexp = LINKEDIN_PATTERN, message = "{validation.linkedin.pattern}")
    private String linkedinUrl;

    public void setName(String name) {
        this.name = TextUtil.cleanLine(name);
    }

    public void setHeadline(String headline) {
        this.headline = TextUtil.optionalLine(headline);
    }

    public void setBio(String bio) {
        this.bio = TextUtil.optional(bio);
    }

    /** Limpia, quita comas (son el separador al guardar) y elimina repetidas sin distinguir mayúsculas. */
    public void setTechnologies(List<String> technologies) {
        if (technologies == null) {
            this.technologies = List.of();
            return;
        }
        Map<String, String> unique = new LinkedHashMap<>();
        technologies.stream()
                .filter(Objects::nonNull)
                .map(value -> TextUtil.cleanLine(value.replace(',', ' ')))
                .filter(value -> value != null && !value.isEmpty())
                .forEach(value -> unique.putIfAbsent(value.toLowerCase(Locale.ROOT), value));
        this.technologies = new ArrayList<>(unique.values());
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = TextUtil.optionalLine(githubUrl);
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = TextUtil.optionalLine(linkedinUrl);
    }
}
