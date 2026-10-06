package com.techstorepro.techstorepro.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.techstorepro.techstorepro.usuario.Usuario;
import com.techstorepro.techstorepro.usuario.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.web.server.ResponseStatusException;

class AuthServiceTests {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder passwords = mock(PasswordEncoder.class);
    private final JwtEncoder encoder = mock(JwtEncoder.class);
    private AuthService auth;

    @BeforeEach
    void setUp() {
        auth = new AuthService(usuarios, passwords, encoder, "techstore-pro", 60);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("signed-token");
        when(encoder.encode(any())).thenReturn(jwt);
    }

    @Test
    void registerNormalizesEmailAndHashesPassword() {
        when(passwords.encode("strong-pass")).thenReturn("bcrypt-hash");
        when(usuarios.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = auth.register(new RegisterRequest("Ana", " ANA@Example.com ", "strong-pass"));

        verify(usuarios).existsByEmail("ana@example.com");
        verify(passwords).encode("strong-pass");
        verify(usuarios).saveAndFlush(argThat(user -> user.getEmail().equals("ana@example.com")
            && user.getPasswordHash().equals("bcrypt-hash")));
        assertEquals("signed-token", response.token());
        assertEquals("ana@example.com", response.user().email());
    }

    @Test
    void registerRejectsExistingEmail() {
        when(usuarios.existsByEmail("ana@example.com")).thenReturn(true);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> auth.register(new RegisterRequest("Ana", "ana@example.com", "strong-pass")));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(usuarios, never()).saveAndFlush(any());
    }

    @Test
    void loginRejectsWrongPassword() {
        when(usuarios.findByEmail("ana@example.com"))
            .thenReturn(Optional.of(new Usuario("Ana", "ana@example.com", "bcrypt-hash")));
        when(passwords.matches("wrong-pass", "bcrypt-hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
            () -> auth.login(new LoginRequest("ana@example.com", "wrong-pass")));
    }
}
