package com.renewsim.backend.auth_service.config;

import com.renewsim.backend.auth_service.application.port.out.TokenBlacklistPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.infrastructure.security.JwtAuthenticationFilter;
import com.renewsim.backend.auth_service.infrastructure.security.SecurityHeadersFilter;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@TestConfiguration
public class SecurityTestBeans {

    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

    @Bean
    public SecurityHeadersFilter securityHeadersFilter() {
        return new SecurityHeadersFilter();
    }

    @Bean
    public TokenBlacklistPort tokenBlacklistPort() {
        return Mockito.mock(TokenBlacklistPort.class);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            TokenProvider tokenProvider,
            TokenBlacklistPort tokenBlacklistPort) {
        return new JwtAuthenticationFilter(tokenProvider, tokenBlacklistPort);
    }
}