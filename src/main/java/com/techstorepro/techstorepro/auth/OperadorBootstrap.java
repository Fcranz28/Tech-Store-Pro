package com.techstorepro.techstorepro.auth;

import com.techstorepro.techstorepro.usuario.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class OperadorBootstrap {
    @Bean
    ApplicationRunner createOperator(UsuarioRepository usuarios, PasswordEncoder encoder,
            @Value("${app.operator.email:}") String email, @Value("${app.operator.password:}") String password) {
        return args -> {
            if (email.isBlank() && password.isBlank()) return;
            if (email.isBlank() || password.length() < 12)
                throw new IllegalArgumentException("OPERATOR_EMAIL y OPERATOR_PASSWORD (mínimo 12 caracteres) son obligatorios juntos");
            if (!usuarios.existsByEmail(email)) usuarios.save(new Usuario("Operador", email, encoder.encode(password), Rol.OPERADOR));
        };
    }
}
