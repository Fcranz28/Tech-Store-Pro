package com.techstorepro.techstorepro.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;

class JwtConfigurationTests {
    private final SecurityConfig config = new SecurityConfig();

    @Test
    void issuedTokenCanBeValidatedWithTheExpectedIssuer() {
        var key = config.jwtSecretKey("test-secret-that-is-longer-than-thirty-two-bytes");
        var encoder = config.jwtEncoder(key);
        var decoder = config.jwtDecoder(key, "techstore-pro");
        var claims = JwtClaimsSet.builder()
            .issuer("techstore-pro")
            .subject("ana@example.com")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        String token = encoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        assertEquals("ana@example.com", decoder.decode(token).getSubject());
        assertThrows(JwtException.class, () -> config.jwtDecoder(key, "another-issuer").decode(token));
    }
}
