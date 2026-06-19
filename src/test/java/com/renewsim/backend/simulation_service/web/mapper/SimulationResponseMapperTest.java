package com.renewsim.backend.simulation_service.web.mapper;

import com.renewsim.backend.simulation_service.application.result.SimulationCreationResultDTO;
import com.renewsim.backend.simulation_service.application.result.SimulationDetailResultDTO;
import com.renewsim.backend.simulation_service.application.result.SimulationHistoryResultDTO;
import com.renewsim.backend.simulation_service.application.service.SimulationCalculator;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulationResponseMapperTest {

    @Mock
    private SimulationCalculator calculator;

    @Test
    @DisplayName("toCreateResponse maps creation result into frontend contract")
    void toCreateResponseMapsCreationResult() {
        SimulationResponseMapper mapper = new SimulationResponseMapper(calculator);

        var response = mapper.toCreateResponse(new SimulationCreationResultDTO(
                15L,
                "Solar Test",
                LocalDateTime.parse("2026-06-08T12:00:00")));

        assertThat(response.id()).isEqualTo(15L);
        assertThat(response.name()).isEqualTo("Solar Test");
        assertThat(response.status()).isEqualTo("completed");
    }

    @Test
    @DisplayName("toResultsResponse maps nested results and financials")
    void toResultsResponseMapsNestedContract() {
        SimulationResponseMapper mapper = new SimulationResponseMapper(calculator);
        SimulationDetailResultDTO detail = new SimulationDetailResultDTO(
                77L,
                "Wind Demo",
                "Cordoba",
                -31.4167,
                -64.1833,
                "WIND",
                80,
                245000,
                120000,
                35.0,
                new ClimateData(450, 9, 40, 18.5, "ERA5", "recent_10yr", "AR"),
                LocalDateTime.parse("2026-06-08T11:00:00"),
                "alice",
                List.of(2L, 3L));

        when(calculator.estimateOpex(any())).thenReturn(3600.0);
        when(calculator.calculateAnnualRevenue(any())).thenReturn(29400.0);
        when(calculator.calculateRoiPercent(any())).thenReturn(21.5);
        when(calculator.calculateRoiYears(any())).thenReturn(5.0);
        when(calculator.calculateNpv(any())).thenReturn(88250.0);
        when(calculator.calculateIrr(any())).thenReturn(13.2);

        var response = mapper.toResultsResponse(detail);

        assertThat(response.location().lat()).isEqualTo(-31.4167);
        assertThat(response.location().country()).isEqualTo("AR");
        assertThat(response.climateData().windSpeed()).isEqualTo(9.0);
        assertThat(response.climateData().temperature()).isEqualTo(18.5);
        assertThat(response.climateData().source()).isEqualTo("ERA5");
        assertThat(response.financials().roi()).isEqualTo(21.5);
        assertThat(response.financials().paybackYears()).isEqualTo(5.0);
        assertThat(response.technology()).isEqualTo("wind");
    }

    @Test
    @DisplayName("toResultsResponse uses null payback when project never recovers investment")
    void toResultsResponseUsesNullPaybackWhenProjectNeverRecoversInvestment() {
        SimulationResponseMapper mapper = new SimulationResponseMapper(calculator);
        SimulationDetailResultDTO detail = new SimulationDetailResultDTO(
                77L,
                "Wind Demo",
                "Cordoba",
                -31.4167,
                -64.1833,
                "WIND",
                80,
                245000,
                120000,
                35.0,
                new ClimateData(450, 9, 40, 18.5, "ERA5", "recent_10yr", "AR"),
                LocalDateTime.parse("2026-06-08T11:00:00"),
                "alice",
                List.of(2L, 3L));

        when(calculator.estimateOpex(any())).thenReturn(3600.0);
        when(calculator.calculateAnnualRevenue(any())).thenReturn(29400.0);
        when(calculator.calculateRoiPercent(any())).thenReturn(-3.0);
        when(calculator.calculateRoiYears(any())).thenReturn(-1.0);
        when(calculator.calculateNpv(any())).thenReturn(-10000.0);
        when(calculator.calculateIrr(any())).thenReturn(-2.0);

        var response = mapper.toResultsResponse(detail);

        assertThat(response.financials().paybackYears()).isNull();
    }

    @Test
    @DisplayName("toUserSummary maps dashboard summary contract")
    void toUserSummaryMapsDashboardContract() {
        SimulationResponseMapper mapper = new SimulationResponseMapper(calculator);

        var response = mapper.toUserSummary(new SimulationHistoryResultDTO(
                90L,
                "Hydro Demo",
                "Mendoza",
                "AR",
                -32.8895,
                -68.8458,
                "HYDRO",
                60,
                88000,
                18.4,
                "completed",
                LocalDateTime.parse("2026-06-08T09:00:00")));

        assertThat(response.name()).isEqualTo("Hydro Demo");
        assertThat(response.technology()).isEqualTo("hydro");
        assertThat(response.location().name()).isEqualTo("Mendoza");
        assertThat(response.location().country()).isEqualTo("AR");
    }
}
