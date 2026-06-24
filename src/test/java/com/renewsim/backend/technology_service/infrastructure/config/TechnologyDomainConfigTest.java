package com.renewsim.backend.technology_service.infrastructure.config;

import com.renewsim.backend.technology_service.domain.service.TechnologyDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TechnologyDomainConfigTest {

    @Test
    @DisplayName("technologyDomainService bean factory should return a domain service")
    void technologyDomainServiceBeanFactoryShouldReturnADomainService() {
        TechnologyDomainConfig config = new TechnologyDomainConfig();

        TechnologyDomainService service = config.technologyDomainService();

        assertThat(service).isNotNull();
    }
}
