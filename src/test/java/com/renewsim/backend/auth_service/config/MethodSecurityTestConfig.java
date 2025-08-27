package com.renewsim.backend.auth_service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@TestConfiguration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityTestConfig {
}
