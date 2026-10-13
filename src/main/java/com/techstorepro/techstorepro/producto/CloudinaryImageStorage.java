package com.techstorepro.techstorepro.producto;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CloudinaryImageStorage {
    private final RestClient client;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    @org.springframework.beans.factory.annotation.Autowired
    public CloudinaryImageStorage(@Value("${app.cloudinary.url:}") String config) {
        this(createClient(), config);
    }

    CloudinaryImageStorage(RestClient client, String config) {
        this.client = client;
        if (config.isBlank()) {
            cloudName = apiKey = apiSecret = "";
            return;
        }
        try {
            URI uri = URI.create(config);
            String[] credentials = uri.getUserInfo().split(":", 2);
            if (!"cloudinary".equals(uri.getScheme()) || !uri.getHost().matches("[a-zA-Z0-9_-]+")
                    || credentials.length != 2 || credentials[0].isBlank() || credentials[1].isBlank()) {
                throw new IllegalArgumentException();
            }
            cloudName = uri.getHost(); apiKey = credentials[0]; apiSecret = credentials[1];
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("La configuración de Cloudinary no es válida.");
        }
    }

    private static RestClient createClient() {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build());
        factory.setReadTimeout(Duration.ofSeconds(40));
        return RestClient.builder().requestFactory(factory).build();
    }

    public String upload(byte[] bytes, String extension) {
        if (apiSecret.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "La subida de imágenes no está disponible. Inténtalo más tarde.");
        String folder = "techstore/productos";
        String publicId = UUID.randomUUID().toString();
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("folder", folder); body.add("public_id", publicId); body.add("timestamp", timestamp);
        body.add("api_key", apiKey);
        body.add("signature", signature("folder=" + folder + "&public_id=" + publicId + "&timestamp=" + timestamp));
        body.add("file", new ByteArrayResource(bytes) {
            @Override public String getFilename() { return publicId + "." + extension; }
        });
        try {
            Map<?, ?> response = client.post().uri("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve().body(Map.class);
            Object value = response == null ? null : response.get("secure_url");
            if (value instanceof String url && url.startsWith("https://res.cloudinary.com/" + cloudName + "/image/upload/")) return url;
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo confirmar la subida de la imagen. Inténtalo de nuevo.");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo subir la imagen. Inténtalo de nuevo.");
        }
    }

    private String signature(String params) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest((params + apiSecret).getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo firmar la subida.");
        }
    }
}
