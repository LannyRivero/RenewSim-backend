package com.renewsim.backend.auth_service.infrastructure.client;

import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

import java.util.Set;

public class FeignAuthConfig {

    private final TokenProvider tokenProvider;

    public FeignAuthConfig(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            String token = tokenProvider.generateServiceToken("auth-service", Set.of("user:write", "user:read"));
            template.header("Authorization", "Bearer " + token);
        };
    }
}
