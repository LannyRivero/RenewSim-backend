package com.renewsim.backend.user_service.infraestructure.client;

import com.renewsim.backend.auth_service.infrastructure.security.JwtTokenProvider;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

import java.util.Set;

public class FeignRoleConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public FeignRoleConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            String token = jwtTokenProvider.generateServiceToken(
                    "user-service",
                    Set.of("role:read")  
            );
            template.header("Authorization", "Bearer " + token);
        };
    }
}
