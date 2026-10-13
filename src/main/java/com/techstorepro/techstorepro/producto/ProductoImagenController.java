package com.techstorepro.techstorepro.producto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/productos/imagenes")
public class ProductoImagenController {
    private final ProductoImagenService images;

    public ProductoImagenController(ProductoImagenService images) {
        this.images = images;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImagenResponse upload(@RequestParam("file") MultipartFile file) {
        return new ImagenResponse(images.upload(file));
    }

    @GetMapping("/{name}")
    public ResponseEntity<Resource> get(@PathVariable String name) {
        Resource image = images.get(name);
        String type = name.endsWith(".png") ? "image/png" : name.endsWith(".webp") ? "image/webp" : "image/jpeg";
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(type))
            .header("X-Content-Type-Options", "nosniff")
            .header("Cache-Control", "public, max-age=31536000, immutable").body(image);
    }

    public record ImagenResponse(String imagenUrl) {}
}
