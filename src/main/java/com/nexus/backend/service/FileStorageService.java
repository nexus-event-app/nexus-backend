package com.nexus.backend.service;

import com.nexus.backend.dto.response.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    UploadResponse storeImage(MultipartFile file);

    void deleteImage(String fileName);
}
