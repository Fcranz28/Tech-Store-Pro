package com.techstorepro.techstorepro.auth;

import com.techstorepro.techstorepro.usuario.Rol;
import com.techstorepro.techstorepro.usuario.Usuario;
import com.techstorepro.techstorepro.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrap {
    @Bean
    ApplicationRunner createAdmin(UsuarioRepository usuarios, PasswordEncoder encoder,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password) {
        return args -> {
            if (email.isBlank() && password.isBlank()) return;
            if (email.isBlank() || password.length() < 12) {
                throw new IllegalArgumentException("ADMIN_EMAIL y ADMIN_PASSWORD (mínimo 12 caracteres) son obligatorios juntos");
            }
            if (usuarios.existsByEmail(email)) return;
            usuarios.save(new Usuario("Administrador", email, encoder.encode(password), Rol.ADMIN));
        };
    }
}
