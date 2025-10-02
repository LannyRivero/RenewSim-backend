package com.renewsim.backend.role_service.infrastructure.config;

import com.renewsim.backend.role_service.domain.service.RoleDomainService;
import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleDomainConfig {

    @Bean
    public RoleDomainService roleDomainService(RoleRepositoryPort roleRepositoryPort) {
        return new RoleDomainService(roleRepositoryPort);
    }
}

