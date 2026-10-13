package com.techstorepro.techstorepro.producto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class ProductoImagenServiceTests {
    @TempDir Path directory;

    @Test
    void uploadsValidatedRasterAndReturnsCloudinaryUrlWithoutWritingLocalFile() throws Exception {
        var cloudinary = mock(CloudinaryImageStorage.class);
        var service = new ProductoImagenService(directory.toString(), cloudinary);
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
        byte[] bytes = output.toByteArray();
        String expected = "https://res.cloudinary.com/demo/image/upload/v1/product.png";
        when(cloudinary.upload(bytes, "png")).thenReturn(expected);
        assertEquals(expected, service.upload(new MockMultipartFile("file", "../../picture.exe", "application/octet-stream", bytes)));
        verify(cloudinary).upload(bytes, "png");
        try (var files = java.nio.file.Files.list(directory)) { assertEquals(0, files.count()); }
    }

    @Test
    void rejectsDisguisedHtmlEmptyOversizedAndTruncatedImages() {
        var cloudinary = mock(CloudinaryImageStorage.class);
        var service = new ProductoImagenService(directory.toString(), cloudinary);
        for (byte[] bytes : new byte[][] {
            "<script>alert(1)</script>".getBytes(), new byte[0], new byte[5 * 1024 * 1024 + 1],
            {(byte)137, 80, 78, 71, 13, 10, 26, 10}
        }) {
            var error = assertThrows(ResponseStatusException.class,
                () -> service.upload(new MockMultipartFile("file", "image.png", "image/png", bytes)));
            assertEquals(400, error.getStatusCode().value());
        }
        verifyNoInteractions(cloudinary);
    }

    @Test
    void cannotReadFilesOutsideUploadDirectoryOrUnknownFiles() {
        var service = new ProductoImagenService(directory.toString(), mock(CloudinaryImageStorage.class));
        for (String name : new String[] { "../../.env", "picture.svg", "00000000-0000-0000-0000-000000000000.png" }) {
            assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.get(name)).getStatusCode().value());
        }
    }
}
