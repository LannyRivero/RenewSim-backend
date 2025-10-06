package com.renewsim.backend.technology_service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.renewsim.backend.technology_service.domain.service.TechnologyDomainService;

/**
 * Configuration class for the Technology domain layer.
 * 
 * This configuration ensures that domain services are available as Spring beans,
 * allowing application or infrastructure layers to inject and use them when required.
 * 
 * Following Clean Architecture, this class should only expose pure domain services 
 * (no dependencies on Spring, JPA, or external frameworks inside the domain itself).
 */
@Configuration
public class TechnologyDomainConfig {

    @Bean
    public TechnologyDomainService technologyDomainService() {
        return new TechnologyDomainService();
    }
}
