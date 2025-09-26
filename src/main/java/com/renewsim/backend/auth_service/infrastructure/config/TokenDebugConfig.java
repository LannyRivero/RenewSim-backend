package com.renewsim.backend.auth_service.infrastructure.config;

import com.renewsim.backend.auth_service.infrastructure.security.JwtTokenProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class TokenDebugConfig {

    @Bean
    CommandLineRunner generateToken(JwtTokenProvider jwtTokenProvider) {
        return args -> {
            String token = jwtTokenProvider.generateServiceToken(
                "auth-service",
                Set.of("users:write") // 👈 ajusta al scope que pida tu UserController
            );
            System.out.println("\n\n🔑 Service Token de prueba:\n" + token + "\n\n");
        };
    }
}

