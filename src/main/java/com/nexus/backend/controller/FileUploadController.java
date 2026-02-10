package com.nexus.backend.controller;

import com.nexus.backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/news-image")
    public ResponseEntity<Map<String, String>> uploadNewsImage(@RequestParam("file") MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen");
        }

        String fileName = fileStorageService.storeFile(file, "");
        String imageUrl = "/uploads/news/" + fileName;

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        response.put("fileName", fileName);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/news-image/{fileName}")
    public ResponseEntity<Void> deleteNewsImage(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName, "");
        return ResponseEntity.noContent().build();
    }
}