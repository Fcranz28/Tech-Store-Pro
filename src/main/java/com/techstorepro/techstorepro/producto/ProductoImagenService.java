package com.techstorepro.techstorepro.producto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductoImagenService {
    private static final long MAX_SIZE = 5 * 1024 * 1024;
    private final Path directory;

    public ProductoImagenService(@Value("${app.upload-dir:./uploads/productos}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public String upload(MultipartFile file) {
        if (file.isEmpty() || file.getSize() > MAX_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una imagen de hasta 5 MB.");
        }
        try {
            byte[] bytes = file.getBytes();
            String extension = extension(bytes);
            if (extension == null || !validImage(bytes, extension)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten imágenes JPEG, PNG y WebP.");
            }
            String name = UUID.randomUUID() + "." + extension;
            Files.createDirectories(directory);
            Files.write(directory.resolve(name), bytes);
            return name;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la imagen.");
        }
    }

    public Resource get(String name) {
        if (!name.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Path file = directory.resolve(name).normalize();
        if (!file.startsWith(directory) || !Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return new FileSystemResource(file);
    }

    private String extension(byte[] b) {
        if (b.length >= 8 && (b[0] & 255) == 137 && b[1] == 80 && b[2] == 78 && b[3] == 71
                && b[4] == 13 && b[5] == 10 && b[6] == 26 && b[7] == 10) return "png";
        if (b.length >= 3 && (b[0] & 255) == 255 && (b[1] & 255) == 216 && (b[2] & 255) == 255) return "jpg";
        if (b.length >= 16 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P'
                && b[12] == 'V' && b[13] == 'P' && b[14] == '8'
                && (b[15] == ' ' || b[15] == 'L' || b[15] == 'X')) return "webp";
        return null;
    }

    private boolean validImage(byte[] bytes, String extension) {
        if (extension.equals("webp")) {
            if (bytes.length < 30) return false;
            long declaredSize = Integer.toUnsignedLong(java.nio.ByteBuffer.wrap(bytes, 4, 4)
                .order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt());
            return declaredSize + 8 == bytes.length;
        }
        try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) return false;
            var reader = readers.next();
            try {
                reader.setInput(input);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                return width > 0 && height > 0 && (long) width * height <= 25_000_000;
            } finally { reader.dispose(); }
        } catch (IOException | RuntimeException e) { return false; }
    }
}
