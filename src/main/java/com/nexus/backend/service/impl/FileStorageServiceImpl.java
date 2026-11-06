package com.nexus.backend.service.impl;

import com.nexus.backend.dto.response.UploadResponse;
import com.nexus.backend.exception.BadRequestException;
import com.nexus.backend.exception.ResourceNotFoundException;
import com.nexus.backend.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    public static final String PUBLIC_PATH = "/uploads/news/";

    private static final Pattern STORED_NAME = Pattern.compile("^[a-f0-9\\-]{36}\\.(jpg|jpeg|png|webp|avif|gif)$");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "avif", "gif");
    private static final Map<String, String> CONTENT_TYPE_BY_EXTENSION = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "avif", "image/avif",
            "gif", "image/gif");
    private static final int SIGNATURE_BYTES = 12;

    private final Path newsDirectory;

    public FileStorageServiceImpl(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.newsDirectory = Paths.get(uploadDir, "news").toAbsolutePath().normalize();
    }

    @PostConstruct
    void createDirectory() {
        try {
            Files.createDirectories(newsDirectory);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo crear el directorio de subidas: " + newsDirectory, ex);
        }
    }

    @Override
    public UploadResponse storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Debes adjuntar un archivo de imagen");
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Formato no permitido. Usa jpg, jpeg, png, webp, avif o gif");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!CONTENT_TYPE_BY_EXTENSION.containsValue(contentType)) {
            throw new BadRequestException("El tipo de contenido del archivo no es una imagen permitida");
        }

        try {
            byte[] header;
            try (InputStream in = file.getInputStream()) {
                header = in.readNBytes(SIGNATURE_BYTES);
            }
            if (!hasValidSignature(extension, header)) {
                throw new BadRequestException("El contenido del archivo no coincide con una imagen válida");
            }

            String fileName = UUID.randomUUID() + "." + extension;
            Path target = newsDirectory.resolve(fileName).normalize();
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target);
            }
            return UploadResponse.builder()
                    .imageUrl(PUBLIC_PATH + fileName)
                    .fileName(fileName)
                    .build();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo guardar la imagen", ex);
        }
    }

    @Override
    public void deleteImage(String fileName) {
        if (fileName == null || !STORED_NAME.matcher(fileName).matches()) {
            throw new BadRequestException("Nombre de archivo no válido");
        }
        Path target = newsDirectory.resolve(fileName).normalize();
        if (!target.startsWith(newsDirectory)) {
            throw new BadRequestException("Nombre de archivo no válido");
        }
        try {
            if (!Files.deleteIfExists(target)) {
                throw new ResourceNotFoundException("Imagen no encontrada");
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo eliminar la imagen", ex);
        }
    }

    private String extractExtension(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dot = originalName.lastIndexOf('.');
        if (dot < 0 || dot == originalName.length() - 1) {
            return "";
        }
        return originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private boolean hasValidSignature(String extension, byte[] header) {
        return switch (extension) {
            case "jpg", "jpeg" -> startsWith(header, 0, 0xFF, 0xD8, 0xFF);
            case "png" -> startsWith(header, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "gif" -> asciiAt(header, 0, "GIF87a") || asciiAt(header, 0, "GIF89a");
            case "webp" -> asciiAt(header, 0, "RIFF") && asciiAt(header, 8, "WEBP");
            case "avif" -> asciiAt(header, 4, "ftyp") && (asciiAt(header, 8, "avif") || asciiAt(header, 8, "avis"));
            default -> false;
        };
    }

    private boolean startsWith(byte[] data, int offset, int... expected) {
        if (data.length < offset + expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((data[offset + i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean asciiAt(byte[] data, int offset, String expected) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.US_ASCII);
        if (data.length < offset + expectedBytes.length) {
            return false;
        }
        return Arrays.equals(Arrays.copyOfRange(data, offset, offset + expectedBytes.length), expectedBytes);
    }
}
