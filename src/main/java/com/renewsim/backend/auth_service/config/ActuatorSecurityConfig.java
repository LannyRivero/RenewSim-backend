package com.renewsim.backend.auth_service.config;

import com.renewsim.backend.auth_service.infrastructure.security.SecurityHeadersFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@Profile("!prod")
public class ActuatorSecurityConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain actuatorSecurityFilterChain(
            HttpSecurity http,
            SecurityHeadersFilter securityHeadersFilter
    ) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(reg -> reg.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .cors(withDefaults());

        http.addFilterAfter(
                securityHeadersFilter,
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}

