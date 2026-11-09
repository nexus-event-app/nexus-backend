package com.nexus.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.time.Duration;

/** Sirve las imágenes subidas: noticias y fotos de perfil. Los nombres son aleatorios, así que se cachean un mes. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String[] FOLDERS = {"news", "avatars"};

    private final String uploadDir;

    public WebConfig(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        for (String folder : FOLDERS) {
            String location = Paths.get(uploadDir, folder).toAbsolutePath().normalize().toUri().toString();
            if (!location.endsWith("/")) {
                location = location + "/";
            }
            registry.addResourceHandler("/uploads/" + folder + "/**")
                    .addResourceLocations(location)
                    .setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
        }
    }
}
