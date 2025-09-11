package com.renewsim.backend.auth_service.infrastructure.client;

import java.util.Set;

import org.springframework.context.annotation.Bean;

import com.renewsim.backend.auth_service.infrastructure.security.JwtTokenProvider;

import feign.Request;
import feign.RequestInterceptor;

public class FeignAuthConfig {
    private final JwtTokenProvider jwtTokenProvider;

    public FeignAuthConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean 
    public RequestInterceptor requestInterceptor() {
        return template -> {
            String token = jwtTokenProvider.generateServiceToken("auth-service",Set.of("user:write","user:read"));
            template.header("Authorization", "Bearer " + token);
        };
    }

}
