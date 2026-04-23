package com.renewsim.backend.auth_service.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.renewsim.backend.auth_service.config.TestSecurityConfig;
import com.renewsim.backend.auth_service.infrastructure.persistence.entity.ActivationTokenEntity;
import com.renewsim.backend.auth_service.infrastructure.persistence.repo.ActivationTokenJpaRepository;
import com.renewsim.backend.shared.testdouble.FakeEmailAdapter;
import com.renewsim.backend.user_service.infrastructure.persistence.entity.UserEntity;
import com.renewsim.backend.user_service.infrastructure.persistence.repo.UserJpaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class RegisterAndActivateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ActivationTokenJpaRepository activationTokenJpaRepository;

    @Autowired
    private FakeEmailAdapter fakeEmailAdapter;

    @BeforeEach
    void setUp() {
        fakeEmailAdapter.clear();
    }

    @Test
    @DisplayName("Should register user and activate account successfully")
    void registerAndActivate_shouldActivateUserAndMarkTokenAsUsed() throws Exception {
        String email = "activate-test@example.com";

        String registerRequest = """
                {
                "email": "activate-test@example.com",
                "password": "Admin@2024",
                "fullName": "Activate Test User"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
                .andExpect(status().isCreated());
        ;

        Optional<UserEntity> createdUserOpt = userJpaRepository.findByEmailIgnoreCase(email);
        assertThat(createdUserOpt).isPresent();

        UserEntity createdUser = createdUserOpt.get();
        assertThat(createdUser.getStatus().name()).isEqualTo("INACTIVE");

        String rawActivationToken = fakeEmailAdapter.getLastActivationToken();
        assertThat(rawActivationToken).isNotBlank();
        assertThat(fakeEmailAdapter.getLastRecipient()).isEqualTo(email);

        String activationRequest = """
                {
                "token": "%s"
                }
                """.formatted(rawActivationToken);

        mockMvc.perform(post("/api/v1/auth/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(activationRequest))
                .andExpect(status().isOk());

        UserEntity activatedUser = userJpaRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(activatedUser.getStatus().name()).isEqualTo("ACTIVE");
        assertThat(activatedUser.getActivatedAt()).isNotNull();

        Optional<ActivationTokenEntity> tokenEntityOpt = activationTokenJpaRepository
                .findTopByUserIdOrderByIssuedAtDesc(activatedUser.getId());

        assertThat(tokenEntityOpt).isPresent();
        assertThat(tokenEntityOpt.get().isUsed()).isTrue();
    }
}