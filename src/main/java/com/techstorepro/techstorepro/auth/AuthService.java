package com.techstorepro.techstorepro.auth;

import com.techstorepro.techstorepro.usuario.Usuario;
import com.techstorepro.techstorepro.usuario.UsuarioRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwords;
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long expirationMinutes;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwords, JwtEncoder jwtEncoder,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.usuarios = usuarios;
        this.passwords = passwords;
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        Usuario usuario;
        try {
            usuario = usuarios.saveAndFlush(new Usuario(request.nombre().trim(), email, passwords.encode(request.password())));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado", exception);
        }
        return tokenFor(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarios.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        if (!passwords.matches(request.password(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        return tokenFor(usuario);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String email) {
        return usuarios.findByEmail(email).map(UserResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private AuthResponse tokenFor(Usuario usuario) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expirationMinutes, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .subject(usuario.getEmail())
            .claim("role", usuario.getRol().name())
            .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new AuthResponse(token, "Bearer", expirationMinutes * 60, UserResponse.from(usuario));
    }
}
