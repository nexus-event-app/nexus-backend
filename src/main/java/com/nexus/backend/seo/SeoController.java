package com.nexus.backend.seo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class SeoController {

    private static final MediaType XML_UTF8 = new MediaType("application", "xml", StandardCharsets.UTF_8);
    private static final MediaType TEXT_UTF8 = new MediaType("text", "plain", StandardCharsets.UTF_8);

    private final SitemapService sitemapService;

    @GetMapping("/sitemap.xml")
    public ResponseEntity<String> sitemap() {
        return ResponseEntity.ok()
                .contentType(XML_UTF8)
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                .body(sitemapService.getSitemapXml());
    }

    @GetMapping("/robots.txt")
    public ResponseEntity<String> robots() {
        return ResponseEntity.ok()
                .contentType(TEXT_UTF8)
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                .body(sitemapService.getRobotsTxt());
    }
}
