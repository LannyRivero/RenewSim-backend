package com.renewsim.backend.auth_service.infrastructure.config;

import com.renewsim.backend.auth_service.infrastructure.security.AuthNoCacheFilter;
import com.renewsim.backend.auth_service.infrastructure.security.JwtAuthenticationFilter;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;
import com.renewsim.backend.auth_service.infrastructure.security.SecurityHeadersFilter;
import com.renewsim.backend.shared.observability.CorrelationIdFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
                        // OpenAPI/Swagger endpoints
                        .requestMatchers(
                                "/api-docs",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**")
                        .permitAll()
                        // Auth endpoints — public
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/email-verification/**").permitAll()
                        // User service internal endpoints
                        .requestMatchers("/api/v1/users/exists").permitAll()
                        .requestMatchers("/api/v1/users/by-username").permitAll()
                        // Actuator & error
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .anyRequest().authenticated())
                .headers(h -> h.cacheControl(HeadersConfigurer.CacheControlConfig::disable))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
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
                        }));

        // 1) Correlation first (MDC & response)
        http.addFilterBefore(correlationIdFilter(), SecurityContextHolderFilter.class);

        // 2) Security headers after correlation
        http.addFilterAfter(securityHeadersFilter, CorrelationIdFilter.class);

        // 3) No-cache for auth endpoints only
        http.addFilterAfter(authNoCacheFilter(), SecurityHeadersFilter.class);

        // 4) Rate limiting for login and refresh endpoints
        http.addFilterAfter(loginRateLimitingFilter, AuthNoCacheFilter.class);

        // 5) JWT auth before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(
                jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return hierarchy;
    }

}    