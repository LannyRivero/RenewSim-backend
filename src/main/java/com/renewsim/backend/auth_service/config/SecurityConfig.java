package com.renewsim.backend.auth_service.config;

import com.renewsim.backend.auth_service.infrastructure.security.AuthNoCacheFilter;
import com.renewsim.backend.auth_service.infrastructure.security.JwtAuthenticationFilter;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;
import com.renewsim.backend.auth_service.infrastructure.security.SecurityHeadersFilter;
import com.renewsim.backend.shared.observability.CorrelationIdFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityHeadersFilter securityHeadersFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LoginRateLimitingFilter loginRateLimitingFilter;

    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    @Bean
    public AuthNoCacheFilter authNoCacheFilter() {
        return new AuthNoCacheFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                .requestMatchers("/actuator/health", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(401);
                    res.setContentType("application/json");
                    // No-cache EXACT strings (stable for tests/clients)
                    res.setHeader("Cache-Control", "no-store, max-age=0");
                    res.setHeader("Pragma", "no-cache");
                    res.setHeader("Expires", "0");
                    res.getWriter().write("""
                        {"status":401,"error":"Unauthorized","message":"Authentication required"}
                        """);
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(403);
                    res.setContentType("application/json");
                    res.setHeader("Cache-Control", "no-store, max-age=0");
                    res.setHeader("Pragma", "no-cache");
                    res.setHeader("Expires", "0");
                    res.getWriter().write("""
                        {"status":403,"error":"Forbidden","message":"Insufficient permissions"}
                        """);
                })
            );

        // 1) CorrelationId → before everything
        http.addFilterBefore(correlationIdFilter(), SecurityContextHolderFilter.class);

        // 2) SecurityHeaders → after CorrelationId
        http.addFilterAfter(securityHeadersFilter, CorrelationIdFilter.class);

        // 3) AuthNoCache → after SecurityHeaders (only affects /api/v1/auth/**)
        http.addFilterAfter(authNoCacheFilter(), SecurityHeadersFilter.class);

        // 4) LoginRateLimiting → after AuthNoCache (only login path internally)
        http.addFilterAfter(loginRateLimitingFilter, AuthNoCacheFilter.class);

        // 5) JwtAuthentication → before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter,
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}

