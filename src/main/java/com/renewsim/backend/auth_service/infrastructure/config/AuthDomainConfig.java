package com.renewsim.backend.auth_service.infrastructure.config;

import com.renewsim.backend.auth_service.domain.service.OtpGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers domain services as Spring beans.
 * Domain services are framework-free — Spring wiring happens here, in infrastructure.
 */
@Configuration
public class AuthDomainConfig {

    @Bean
    public OtpGenerator otpGenerator() {
        return new OtpGenerator();
    }
}