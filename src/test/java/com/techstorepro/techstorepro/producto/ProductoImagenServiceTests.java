package com.techstorepro.techstorepro.producto;

import static org.junit.jupiter.api.Assertions.*;

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
    void savesRasterWithGeneratedNameAndCanReadItBack() throws Exception {
        var service = new ProductoImagenService(directory.toString());
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
        byte[] bytes = output.toByteArray();
        String name = service.upload(new MockMultipartFile("file", "../../picture.exe", "application/octet-stream", bytes));
        assertTrue(name.endsWith(".png"));
        assertFalse(name.contains("picture"));
        assertArrayEquals(bytes, service.get(name).getContentAsByteArray());
    }

    @Test
    void rejectsDisguisedHtmlEmptyOversizedAndTruncatedImages() {
        var service = new ProductoImagenService(directory.toString());
        for (byte[] bytes : new byte[][] {
            "<script>alert(1)</script>".getBytes(), new byte[0], new byte[5 * 1024 * 1024 + 1],
            {(byte)137, 80, 78, 71, 13, 10, 26, 10}
        }) {
            var error = assertThrows(ResponseStatusException.class,
                () -> service.upload(new MockMultipartFile("file", "image.png", "image/png", bytes)));
            assertEquals(400, error.getStatusCode().value());
        }
    }

    @Test
    void cannotReadFilesOutsideUploadDirectoryOrUnknownFiles() {
        var service = new ProductoImagenService(directory.toString());
        for (String name : new String[] { "../../.env", "picture.svg", "00000000-0000-0000-0000-000000000000.png" }) {
            assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.get(name)).getStatusCode().value());
        }
    }
}
