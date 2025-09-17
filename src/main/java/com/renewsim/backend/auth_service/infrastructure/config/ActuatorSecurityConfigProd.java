package com.renewsim.backend.auth_service.infrastructure.config;

import com.renewsim.backend.auth_service.infrastructure.security.JwtAuthenticationFilter;
import com.renewsim.backend.auth_service.infrastructure.security.SecurityHeadersFilter;
import com.renewsim.backend.shared.observability.CorrelationIdFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@Profile("prod")
@RequiredArgsConstructor
public class ActuatorSecurityConfigProd {

        @Bean
        @Order(0)
        public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http,
                        SecurityHeadersFilter securityHeadersFilter,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        CorrelationIdFilter correlationIdFilter) throws Exception {
                http
                                .securityMatcher("/actuator/**")
                                .authorizeHttpRequests(reg -> reg
                                                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                                                .anyRequest().hasAuthority("SCOPE_actuator:read"))
                                .csrf(csrf -> csrf.disable())
                                .cors(withDefaults());
                http.addFilterBefore(correlationIdFilter, SecurityContextHolderFilter.class);
                http.addFilterAfter(securityHeadersFilter, CorrelationIdFilter.class);
                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
