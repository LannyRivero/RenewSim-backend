package com.renewsim.backend.simulation_service.create.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.infrastructure.security.JwtTokenProvider;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;
import com.renewsim.backend.config.TestSecurityConfig;
import com.renewsim.backend.simulation_service.create.application.port.in.CreateRealSimulationUseCase;
import com.renewsim.backend.simulation_service.create.application.port.in.CreateSimulationFromScenarioUseCase;
import com.renewsim.backend.simulation_service.create.web.dto.CreateSimulationFromScenarioRequestDTO;
import com.renewsim.backend.simulation_service.create.web.dto.CreateSolarSimulationRequestDTO;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class CreateSimulationControllerTest {

    private static final String AUTHORIZED_TOKEN = "authorized-token";
    private static final String ROLE_ONLY_TOKEN = "role-only-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private LoginRateLimitingFilter loginRateLimitingFilter;

    @MockitoBean
    private CreateRealSimulationUseCase createRealSimulationUseCase;

    @MockitoBean
    private CreateSimulationFromScenarioUseCase createSimulationFromScenarioUseCase;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            invocation.getArgument(0, ServletRequest.class);
            invocation.getArgument(1, ServletResponse.class);
            invocation.getArgument(2, FilterChain.class).doFilter(
                    invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(loginRateLimitingFilter).doFilter(any(), any(), any());

        when(jwtTokenProvider.validate(AUTHORIZED_TOKEN))
                .thenReturn(Optional.of(new AuthenticatedUser(
                        "alice",
                        Set.of("USER"),
                        Set.of("write:simulations", "read:simulations"))));

        when(jwtTokenProvider.validate(ROLE_ONLY_TOKEN))
                .thenReturn(Optional.of(new AuthenticatedUser(
                        "bob",
                        Set.of("USER"),
                        Set.of("read:simulations"))));
    }

    @Test
    @DisplayName("createSimulationFromScenario returns 201 for a user with write scope")
    void createSimulationFromScenarioReturns201ForUserWithWriteScope() throws Exception {
        when(createSimulationFromScenarioUseCase.createSimulationFromScenario(any())).thenReturn(sampleResult());

        mockMvc.perform(post("/api/v1/simulations/from-scenario")
                        .header(HttpHeaders.AUTHORIZATION, bearer(AUTHORIZED_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSimulationFromScenarioRequestDTO(
                                7L,
                                null,
                                new CreateSolarSimulationRequestDTO.LocationDTO(
                                        "Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", "ES")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("55"))
                .andExpect(jsonPath("$.technical.annualGenerationKwh").value(457200));

        verify(createSimulationFromScenarioUseCase).createSimulationFromScenario(any());
    }

    @Test
    @DisplayName("createSimulationFromScenario returns 403 for a role-only user without write scope")
    void createSimulationFromScenarioReturns403ForRoleOnlyUserWithoutWriteScope() throws Exception {
        mockMvc.perform(post("/api/v1/simulations/from-scenario")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ROLE_ONLY_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSimulationFromScenarioRequestDTO(
                                7L,
                                null,
                                new CreateSolarSimulationRequestDTO.LocationDTO(
                                        "Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", "ES")))))
                .andExpect(status().isForbidden());
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private SimulationDetailsResult sampleResult() {
        return new SimulationDetailsResult(
                "55", "completed", "2026-06-30T14:00:00Z", "2026-06-30T14:00:00Z", "solar-spain-v1", "solar",
                new SimulationDetailsResult.ResolvedLocation("Sevilla, Andalucia, ES", "Sevilla", "Andalucia", "Spain", "ES", 37.3891, -5.9845, "Europe/Madrid"),
                new SimulationDetailsResult.Summary("viable_with_reservations", "headline", "summary", List.of(new SimulationDetailsResult.RecommendationReason("resource", "positive", "msg"))),
                new SimulationDetailsResult.Input("Solar - Sevilla", "solar", new SimulationDetailsResult.Location("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", "ES"), new SimulationDetailsResult.SystemSpec(300, 0.81, 0.5, 99, new SimulationDetailsResult.LossesPct(2, 6, 1, 3, 1)), new SimulationDetailsResult.Demand(120000, List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d)), new SimulationDetailsResult.Economics("EUR", 315000, 7200, 0.18, 0.07, 8, 20)),
                new SimulationDetailsResult.Technical(457200, List.of(24800d, 29100d), 1524, 0.81, 17.4, 72.3, 31.5, new SimulationDetailsResult.ResourceSeries("PVGIS", "2005-2020", List.of(71d), List.of(10d)), new SimulationDetailsResult.LossesSummary(2, 6, 1, 3, 1, 13), List.of(new SimulationDetailsResult.MonthlyEnergyBalanceItem("Jan", 24800, 10000, 10000, 14800, 0))),
                new SimulationDetailsResult.Financial("EUR", 68700, 8800, 70300, 6.9, 8.7, 121500, 11.4, 0.071, List.of(new SimulationDetailsResult.FinancialYearItem(0, 0, 0, 0, 0, -315000, -315000, -315000))),
                new SimulationDetailsResult.Assumptions(8, 20, 0.5, 0.18, 0.07, "PVGIS", "2005-2020"),
                List.of(new SimulationDetailsResult.SimulationWarning("info", "MONTHLY_PROFILE_USER_SUPPLIED", "warning")));
    }
}
