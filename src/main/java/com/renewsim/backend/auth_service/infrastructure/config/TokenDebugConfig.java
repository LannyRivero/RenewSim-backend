package com.renewsim.backend.auth_service.infrastructure.config;

import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Set;

@Configuration
@Profile("local")
public class TokenDebugConfig {

    @Bean
    CommandLineRunner generateToken(TokenProvider tokenProvider) {
        return args -> {
            String token = tokenProvider.generateServiceToken("auth-service", Set.of("users:write"));
            System.out.println("\n\nService test token:\n" + token + "\n\n");
        };
    }
}
