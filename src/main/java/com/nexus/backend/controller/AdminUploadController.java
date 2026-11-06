package com.nexus.backend.controller;

import com.nexus.backend.dto.response.UploadResponse;
import com.nexus.backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/uploads/image")
@RequiredArgsConstructor
public class AdminUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<UploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(fileStorageService.storeImage(file));
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<Void> deleteImage(@PathVariable String fileName) {
        fileStorageService.deleteImage(fileName);
        return ResponseEntity.noContent().build();
    }
}
