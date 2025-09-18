package com.renewsim.backend.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.renewsim.backend.role.Role;
import com.renewsim.backend.role.RoleRepository;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.technologyComparison.TechnologyComparison;
import com.renewsim.backend.technologyComparison.TechnologyComparisonRepository;
import com.renewsim.backend.user.User;
import com.renewsim.backend.user.UserRepository;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    public SecurityIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TechnologyComparisonRepository technologyComparisonRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        technologyComparisonRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setName(RoleName.USER);
        roleRepository.save(userRole);

        User user = new User();
        user.setUsername("test-user");
        user.setPassword(passwordEncoder.encode("test-password"));
        user.setRoles(Collections.singleton(userRole));
        userRepository.save(user);

        TechnologyComparison tech = new TechnologyComparison();
        tech.setTechnologyName("Solar Panel");
        tech.setEnergyType("SOLAR");
        tech.setInstallationCost(1000.0);
        tech.setMaintenanceCost(100.0);
        tech.setEfficiency(85.0);
        tech.setEnergyProduction(5000.0);
        tech.setCo2Reduction(500.0);
        technologyComparisonRepository.save(tech);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private String generateJwtToken(String scope) {

        String secret = "This is a very secure secret for testing purpose and secure";
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        return Jwts.builder()
                .setSubject("test-user")
                .claim("scope", scope)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    @DisplayName("Public route /api/v1/auth/login should be accessible without authentication")
    void shouldAllowAccessToLoginWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"test-user\", \"password\": \"test-password\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected GET /api/v1/simulation requires scope SCOPE_read:simulations")
    @WithMockUser(username = "test-user", authorities = { "SCOPE_read:simulations" })
    void shouldAllowAccessToSimulationReadWithProperScope() throws Exception {
        mockMvc.perform(get("/api/v1/simulation/user"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected route should return 403 if user lacks required scope")
    @WithMockUser(authorities = { "SCOPE_read:simulations" })
    void shouldDenyAccessToAdminWithoutProperScope() throws Exception {
        mockMvc.perform(get("/api/v1/admin/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("JWT token with invalid scope should be forbidden")
    void shouldRejectJwtTokenWithInvalidScope() throws Exception {
        String token = generateJwtToken("invalid:scope");

        mockMvc.perform(get("/api/v1/simulation/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Invalid JWT token should be unauthorized")
    void shouldRejectInvalidJwtToken() throws Exception {
        mockMvc.perform(get("/api/v1/simulation/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Anonymous user should not access protected simulation route")
    void shouldDenyAnonymousAccessToSimulation() throws Exception {
        mockMvc.perform(get("/api/v1/simulation/test"))
                .andExpect(status().isUnauthorized());
    }
}
