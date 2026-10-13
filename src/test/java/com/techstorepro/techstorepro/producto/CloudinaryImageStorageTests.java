package com.techstorepro.techstorepro.producto;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

class CloudinaryImageStorageTests {
    @Test void returnsSecureUrlFromSignedMultipartUpload() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.cloudinary.com/v1_1/demo/image/upload"))
            .andExpect(method(org.springframework.http.HttpMethod.POST))
            .andExpect(request -> assertTrue(request.getHeaders().getContentType().isCompatibleWith(MediaType.MULTIPART_FORM_DATA)))
            .andRespond(withSuccess("{\"secure_url\":\"https://res.cloudinary.com/demo/image/upload/v1/sample.png\"}", MediaType.APPLICATION_JSON));
        var storage = new CloudinaryImageStorage(builder.build(), "cloudinary://test-key:test-secret@demo");
        assertEquals("https://res.cloudinary.com/demo/image/upload/v1/sample.png", storage.upload(new byte[]{1}, "png"));
        server.verify();
    }
    @Test void masksProviderErrorAndRejectsMissingConfiguration() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(anything()).andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("provider secret details"));
        var storage = new CloudinaryImageStorage(builder.build(), "cloudinary://test-key:test-secret@demo");
        var error = assertThrows(ResponseStatusException.class, () -> storage.upload(new byte[]{1}, "png"));
        assertEquals(502, error.getStatusCode().value());
        assertFalse(error.getMessage().contains("secret"));
        assertEquals(503, assertThrows(ResponseStatusException.class,
            () -> new CloudinaryImageStorage("").upload(new byte[]{1}, "png")).getStatusCode().value());
    }
}
