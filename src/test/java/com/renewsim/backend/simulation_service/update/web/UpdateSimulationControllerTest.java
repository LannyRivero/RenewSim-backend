package com.renewsim.backend.simulation_service.update.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.infrastructure.security.JwtTokenProvider;
import com.renewsim.backend.auth_service.infrastructure.security.LoginRateLimitingFilter;
import com.renewsim.backend.config.TestSecurityConfig;
import com.renewsim.backend.simulation_service.create.web.dto.CreateSimulationRequestDTO;
import com.renewsim.backend.simulation_service.create.web.dto.SimulationLocationRequestDTO;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationStatusTransitionException;
import com.renewsim.backend.simulation_service.domain.model.SimulationStatus;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.update.application.port.in.UpdateSimulationUseCase;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class UpdateSimulationControllerTest {

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
  private UpdateSimulationUseCase updateSimulationUseCase;

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
  @DisplayName("updateSimulation returns 200 with updated results for a user with write scope")
  void updateSimulationReturns200ForUserWithWriteScope() throws Exception {
    when(updateSimulationUseCase.updateSimulation(any(), any(), anyBoolean())).thenReturn(sampleResult());

    mockMvc.perform(put("/api/v1/simulations/55")
        .header(HttpHeaders.AUTHORIZATION, bearer(AUTHORIZED_TOKEN))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("55"))
        .andExpect(jsonPath("$.technical.annualGenerationKwh").value(457200));

    verify(updateSimulationUseCase).updateSimulation(any(), any(), anyBoolean());
  }

  @Test
  @DisplayName("updateSimulation returns 403 for a role-only user without write scope")
  void updateSimulationReturns403ForRoleOnlyUserWithoutWriteScope() throws Exception {
    mockMvc.perform(put("/api/v1/simulations/55")
        .header(HttpHeaders.AUTHORIZATION, bearer(ROLE_ONLY_TOKEN))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("updateSimulation returns 409 when the simulation status prevents editing")
  void updateSimulationReturns409ForTerminalStatus() throws Exception {
    when(updateSimulationUseCase.updateSimulation(any(), any(), anyBoolean()))
        .thenThrow(new InvalidSimulationStatusTransitionException("update", SimulationStatus.DELETED));

    mockMvc.perform(put("/api/v1/simulations/55")
        .header(HttpHeaders.AUTHORIZATION, bearer(AUTHORIZED_TOKEN))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest())))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errorCode").value("RESOURCE_CONFLICT"));
  }

  @Test
  @DisplayName("updateSimulation returns 400 when the request body is invalid")
  void updateSimulationReturns400ForInvalidRequest() throws Exception {
    mockMvc.perform(put("/api/v1/simulations/55")
        .header(HttpHeaders.AUTHORIZATION, bearer(AUTHORIZED_TOKEN))
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "name": "",
              "energyType": "solar",
              "location": {
                "label": "Sevilla, Andalucia, ES",
                "lat": 37.3891,
                "lon": -5.9845,
                "country": "Spain",
                "countryCode": "ES"
              },
              "system": {
                "installedCapacityKw": 300,
                "performanceRatio": 0.81,
                "degradationRateAnnualPct": 0.5,
                "availabilityPct": 99,
                "lossesPct": {
                  "inverter": 2,
                  "temperature": 6,
                  "wiring": 1,
                  "soiling": 3,
                  "other": 1
                }
              },
              "demand": {
                "annualConsumptionKwh": 120000,
                "monthlyConsumptionKwh": [10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000]
              },
              "economics": {
                "currency": "EUR",
                "capexTotal": 315000,
                "opexAnnual": 7200,
                "electricityPurchasePricePerKwh": 0.18,
                "exportPricePerKwh": 0.07,
                "discountRatePct": 8,
                "projectLifetimeYears": 20
              },
              "technologyIds": []
            }
            """))
        .andExpect(status().isBadRequest());
  }

  private static String bearer(String token) {
    return "Bearer " + token;
  }

  private CreateSimulationRequestDTO validRequest() {
    return new CreateSimulationRequestDTO(
        "Solar - Sevilla",
        "solar",
        new SimulationLocationRequestDTO(
            "Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", "ES"),
        new CreateSimulationRequestDTO.SystemDTO(
            300, 0.81, 0.5, 99,
            new CreateSimulationRequestDTO.LossesPctDTO(2, 6, 1, 3, 1)),
        new CreateSimulationRequestDTO.DemandDTO(
            120000,
            List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                10000d, 10000d, 10000d)),
        new CreateSimulationRequestDTO.EconomicsDTO(
            "EUR", 315000, 7200, 0.18, 0.07, 8, 20),
        List.of(11L, 12L));
  }

  private SimulationDetailsResult sampleResult() {
    return new SimulationDetailsResult(
        "55", "completed", "2026-06-30T14:00:00Z", "2026-06-30T14:00:00Z", "solar-spain-v1",
        "solar",
        new SimulationDetailsResult.ResolvedLocation("Sevilla, Andalucia, ES", "Sevilla",
            "Andalucia", "Spain", "ES", 37.3891, -5.9845, "Europe/Madrid"),
        new SimulationDetailsResult.Summary("viable_with_reservations", "headline", "summary",
            List.of(new SimulationDetailsResult.RecommendationReason("resource",
                "positive", "msg"))),
        new SimulationDetailsResult.Input("Solar - Sevilla", "solar",
            new SimulationDetailsResult.Location("Sevilla, Andalucia, ES", 37.3891,
                -5.9845, "Spain", "ES"),
            new SimulationDetailsResult.SystemSpec(300, 0.81, 0.5, 99,
                new SimulationDetailsResult.LossesPct(2, 6, 1, 3, 1)),
            new SimulationDetailsResult.Demand(120000,
                List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                    10000d, 10000d, 10000d, 10000d, 10000d,
                    10000d)),
            new SimulationDetailsResult.Economics("EUR", 315000, 7200, 0.18, 0.07,
                8, 20)),
        new SimulationDetailsResult.Technical(457200, List.of(24800d, 29100d), 1524, 0.81, 17.4,
            72.3, 31.5,
            new SimulationDetailsResult.ResourceSeries("PVGIS", "2005-2020",
                List.of(71d), List.of(10d)),
            new SimulationDetailsResult.LossesSummary(2, 6, 1, 3, 1, 13),
            List.of(new SimulationDetailsResult.MonthlyEnergyBalanceItem("Jan",
                24800, 10000, 10000, 14800, 0))),
        new SimulationDetailsResult.Financial("EUR", 68700, 8800, 70300, 6.9, 8.7, 121500, 11.4,
            0.071,
            List.of(new SimulationDetailsResult.FinancialYearItem(0, 0, 0, 0, 0,
                -315000, -315000, -315000))),
        new SimulationDetailsResult.Assumptions(8, 20, 0.5, 0.18, 0.07, "PVGIS", "2005-2020"),
        List.of(new SimulationDetailsResult.SimulationWarning("info",
            "MONTHLY_PROFILE_USER_SUPPLIED", "warning")));
  }
}
