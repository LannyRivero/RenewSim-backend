package com.renewsim.backend.simulation_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.renewsim.backend.simulation_service.application.port.out.SimulationRepositoryPort;
import com.renewsim.backend.simulation_service.application.result.SimulationDashboardSummaryResult;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.Budget;
import com.renewsim.backend.simulation_service.domain.model.vo.CO2Reduction;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyOutput;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyType;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectSize;

@ExtendWith(MockitoExtension.class)
class SimulationDashboardQueryServiceTest {

    @Mock
    private SimulationRepositoryPort repository;
    @Mock
    private SimulationCalculator calculator;

    @Test
    @DisplayName("getDashboardSummary returns efficiency KPIs and target-vs-actual metrics")
    void getDashboardSummaryReturnsEfficiencyAndTargets() {
        SimulationDashboardQueryService service = new SimulationDashboardQueryService(repository, calculator);

        Simulation solar = new Simulation(
                1L,
                "Solar One",
                "Mendoza",
                -32.8895,
                -68.8458,
                EnergyType.SOLAR,
                new ProjectSize(100),
                new Budget(100000),
                new EnergyOutput(200000),
                new CO2Reduction(140),
                new ClimateData(900, 4, 20),
                List.of(),
                "alice",
                LocalDateTime.parse("2026-05-22T12:00:00"));

        Simulation wind = new Simulation(
                2L,
                "Wind Two",
                "Cordoba",
                -31.4167,
                -64.1833,
                EnergyType.WIND,
                new ProjectSize(50),
                new Budget(90000),
                new EnergyOutput(120000),
                new CO2Reduction(84),
                new ClimateData(500, 8, 10),
                List.of(),
                "alice",
                LocalDateTime.parse("2026-05-23T12:00:00"));

        when(repository.findAllByCreatedBy("alice")).thenReturn(List.of(solar, wind));
        when(calculator.calculateCapacityFactor(solar)).thenReturn(25.0);
        when(calculator.calculateCapacityFactor(wind)).thenReturn(40.0);
        when(calculator.calculateRoiPercent(any(Simulation.class))).thenReturn(12.5, -2.0);

        SimulationDashboardSummaryResult summary = service.getDashboardSummary("alice");

        assertThat(summary.stats().totalSimulations()).isEqualTo(2);
        assertThat(summary.stats().totalEnergyGeneratedKwh()).isEqualTo(320000.0);
        assertThat(summary.stats().totalCo2SavedKg()).isEqualTo(224000.0);
        assertThat(summary.stats().averageRoiPercent()).isEqualTo(5.25);

        assertThat(summary.efficiencyMetrics()).hasSize(3);
        assertThat(summary.efficiencyMetrics().get(0).label()).isEqualTo("Capacity factor");
        assertThat(summary.efficiencyMetrics().get(0).value()).isEqualTo("32.5%");
        assertThat(summary.efficiencyMetrics().get(1).label()).isEqualTo("Specific yield");
        assertThat(summary.efficiencyMetrics().get(1).value()).isEqualTo("2133 kWh/kW-year");
        assertThat(summary.efficiencyMetrics().get(2).label()).isEqualTo("Positive ROI");
        assertThat(summary.efficiencyMetrics().get(2).value()).isEqualTo("50.0%");

        assertThat(summary.targetVsActual()).hasSize(3);
        assertThat(summary.targetVsActual().get(0).label()).isEqualTo("Energy output");
        assertThat(summary.targetVsActual().get(0).actual()).isEqualTo(320000.0);
        assertThat(summary.targetVsActual().get(0).target()).isEqualTo(1314000.0);
        assertThat(summary.targetVsActual().get(0).unit()).isEqualTo("kWh");

        assertThat(summary.targetVsActual().get(1).label()).isEqualTo("CO2 reduction");
        assertThat(summary.targetVsActual().get(1).actual()).isEqualTo(224000.0);
        assertThat(summary.targetVsActual().get(1).target()).isCloseTo(919800.0, org.assertj.core.data.Offset.offset(0.001));
        assertThat(summary.targetVsActual().get(1).unit()).isEqualTo("kg");

        assertThat(summary.targetVsActual().get(2).label()).isEqualTo("ROI");
        assertThat(summary.targetVsActual().get(2).actual()).isEqualTo(5.25);
        assertThat(summary.targetVsActual().get(2).target()).isZero();
        assertThat(summary.targetVsActual().get(2).unit()).isEqualTo("%");
    }
}
