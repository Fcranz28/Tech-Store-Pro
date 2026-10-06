package com.techstorepro.techstorepro.producto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/productos/imagenes")
public class ProductoImagenController {
    private final ProductoImagenService images;
    private final String publicUrl;

    public ProductoImagenController(ProductoImagenService images,
            @Value("${app.public-url:http://localhost:8080}") String publicUrl) {
        this.images = images;
        this.publicUrl = publicUrl.replaceAll("/+$", "");
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImagenResponse upload(@RequestParam("file") MultipartFile file) {
        return new ImagenResponse(publicUrl + "/productos/imagenes/" + images.upload(file));
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
