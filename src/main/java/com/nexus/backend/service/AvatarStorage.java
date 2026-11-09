package com.nexus.backend.service;

import com.nexus.backend.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Fotos de perfil. La imagen subida nunca se guarda tal cual: se recorta al centro en cuadrado, se reduce a
 * {@value #SIZE}×{@value #SIZE} px y se vuelve a generar como JPEG. Así pesa poco y se eliminan los metadatos que
 * traen las fotos de móvil (ubicación GPS, modelo del teléfono...).
 */
@Slf4j
@Service
public class AvatarStorage {

    public static final String PUBLIC_PATH = "/uploads/avatars/";
    static final int SIZE = 256;
    /** Lado máximo de la imagen original, para no descomprimir imágenes enormes en memoria. */
    private static final int MAX_SOURCE_SIDE = 6000;
    private static final Set<String> FORMATS = Set.of("jpeg", "png", "gif");
    private static final Pattern STORED_URL = Pattern.compile("^" + Pattern.quote(PUBLIC_PATH) + "([a-f0-9\\-]{36}\\.jpg)$");

    private final Path directory;

    public AvatarStorage(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.directory = Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();
    }

    public Path directory() {
        return directory;
    }

    @PostConstruct
    void createDirectory() {
        try {
            Files.createDirectories(directory);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo crear el directorio de fotos de perfil: " + directory, ex);
        }
    }

    /** Procesa y guarda la foto. Devuelve su ruta pública. */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("error.image.required");
        }
        BufferedImage avatar = square(read(file));
        String fileName = UUID.randomUUID() + ".jpg";
        Path target = directory.resolve(fileName);
        try {
            writeJpeg(avatar, target);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo guardar la foto de perfil", ex);
        }
        return PUBLIC_PATH + fileName;
    }

    /** Borra una foto guardada antes. Ignora rutas que no sean de este almacén. */
    public void delete(String publicUrl) {
        if (publicUrl == null) {
            return;
        }
        var matcher = STORED_URL.matcher(publicUrl);
        if (!matcher.matches()) {
            return;
        }
        try {
            Files.deleteIfExists(directory.resolve(matcher.group(1)));
        } catch (IOException ex) {
            log.warn("No se pudo borrar la foto de perfil {}: {}", publicUrl, ex.getMessage());
        }
    }

    private BufferedImage read(MultipartFile file) {
        try (InputStream in = file.getInputStream(); ImageInputStream stream = ImageIO.createImageInputStream(in)) {
            Iterator<ImageReader> readers = stream == null ? null : ImageIO.getImageReaders(stream);
            if (readers == null || !readers.hasNext()) {
                throw new BadRequestException("error.avatar.format");
            }
            ImageReader reader = readers.next();
            try {
                if (!FORMATS.contains(reader.getFormatName().toLowerCase(Locale.ROOT))) {
                    throw new BadRequestException("error.avatar.format");
                }
                reader.setInput(stream, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > MAX_SOURCE_SIDE || height > MAX_SOURCE_SIDE) {
                    throw new BadRequestException("error.avatar.dimensions");
                }
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException ex) {
            if (ex instanceof BadRequestException badRequest) {
                throw badRequest;
            }
            throw new BadRequestException("error.image.invalid");
        }
    }

    /** Recorte cuadrado centrado y reducido, sobre fondo blanco (JPEG no admite transparencia). */
    private static BufferedImage square(BufferedImage source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;
        int size = Math.min(SIZE, side);
        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, size, size);
            graphics.drawImage(source, 0, 0, size, size, x, y, x + side, y + side, null);
        } finally {
            graphics.dispose();
        }
        return result;
    }

    private static void writeJpeg(BufferedImage image, Path target) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam params = writer.getDefaultWriteParam();
        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(0.85f);
        try (ImageOutputStream out = ImageIO.createImageOutputStream(Files.newOutputStream(target))) {
            writer.setOutput(out);
            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }
    }
}
