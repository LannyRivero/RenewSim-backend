package com.renewsim.backend.simulation_service.domain.model;

import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationCompletionException;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationCreatorException;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationNameException;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationStatusTransitionException;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.Currency;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectLifetime;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimulationTest {

    @Test
    @DisplayName("create rejects blank simulation name")
    void createRejectsBlankSimulationName() {
        assertThatThrownBy(() -> Simulation.create(
                " ",
                Technology.solar(),
                validLocation(),
                validSystem(),
                validDemand(),
                validEconomics(),
                List.of(),
                null,
                "alice"))
                .isInstanceOf(InvalidSimulationNameException.class)
                .hasMessageContaining("name");
    }

    @Test
    @DisplayName("create rejects blank creator")
    void createRejectsBlankCreator() {
        assertThatThrownBy(() -> Simulation.create(
                "Solar - Sevilla",
                Technology.solar(),
                validLocation(),
                validSystem(),
                validDemand(),
                validEconomics(),
                List.of(),
                null,
                " "))
                .isInstanceOf(InvalidSimulationCreatorException.class)
                .hasMessageContaining("creator");
    }

    @Test
    @DisplayName("complete rejects null completion")
    void completeRejectsNullCompletion() {
        Simulation simulation = validSimulation();

        assertThatThrownBy(() -> simulation.complete(null))
                .isInstanceOf(InvalidSimulationCompletionException.class)
                .hasMessageContaining("required");
    }

    @Test
    @DisplayName("complete updates status and result fields")
    void completeUpdatesStatusAndResultFields() {
        Simulation simulation = validSimulation();
        SimulationCompletion completion = new SimulationCompletion(
                "{\"result\":true}",
                457200.0,
                82000.0,
                121500.0,
                14.2,
                "recommended",
                List.of(21L, 22L));

        simulation.complete(completion);

        assertThat(simulation.getStatus()).isEqualTo(SimulationStatus.COMPLETED);
        assertThat(simulation.getResultSnapshot()).isEqualTo("{\"result\":true}");
        assertThat(simulation.getRecommendation()).isEqualTo("recommended");
        assertThat(simulation.getTechnologyIds()).containsExactly(21L, 22L);
        assertThat(simulation.getUpdatedAt()).isAfterOrEqualTo(simulation.getCreatedAt());
    }

    @Test
    @DisplayName("complete rejects non draft simulations")
    void completeRejectsNonDraftSimulations() {
        Simulation simulation = validSimulation();
        simulation.delete();

        assertThatThrownBy(() -> simulation.complete(new SimulationCompletion(
                "{\"result\":true}",
                457200.0,
                82000.0,
                121500.0,
                14.2,
                "recommended",
                List.of(21L, 22L))))
                .isInstanceOf(InvalidSimulationStatusTransitionException.class)
                .hasMessageContaining("complete");
    }

    @Test
    @DisplayName("update recomputes status and result fields on an existing simulation")
    void updateRecomputesStatusAndResultFields() {
        Simulation simulation = validSimulation();
        simulation.complete(new SimulationCompletion(
                "{\"result\":\"old\"}",
                1000.0,
                100.0,
                50.0,
                5.0,
                "old",
                List.of(11L, 12L)));

        simulation.update(new SimulationCompletion(
                "{\"result\":\"new\"}",
                457200.0,
                82000.0,
                121500.0,
                14.2,
                "recommended",
                List.of(21L, 22L)));

        assertThat(simulation.getStatus()).isEqualTo(SimulationStatus.COMPLETED);
        assertThat(simulation.getResultSnapshot()).isEqualTo("{\"result\":\"new\"}");
        assertThat(simulation.getRecommendation()).isEqualTo("recommended");
        assertThat(simulation.getTechnologyIds()).containsExactly(21L, 22L);
        assertThat(simulation.getUpdatedAt()).isAfterOrEqualTo(simulation.getCreatedAt());
    }

    @Test
    @DisplayName("update rejects null completion")
    void updateRejectsNullCompletion() {
        Simulation simulation = validSimulation();

        assertThatThrownBy(() -> simulation.update(null))
                .isInstanceOf(InvalidSimulationCompletionException.class)
                .hasMessageContaining("required");
    }

    @Test
    @DisplayName("update rejects deleted simulations")
    void updateRejectsDeletedSimulations() {
        Simulation simulation = validSimulation();
        simulation.delete();

        assertThatThrownBy(() -> simulation.update(new SimulationCompletion(
                "{\"result\":true}",
                457200.0,
                82000.0,
                121500.0,
                14.2,
                "recommended",
                List.of(21L, 22L))))
                .isInstanceOf(InvalidSimulationStatusTransitionException.class)
                .hasMessageContaining("update");
    }

    private Simulation validSimulation() {
        return Simulation.create(
                "Solar - Sevilla",
                Technology.solar(),
                validLocation(),
                validSystem(),
                validDemand(),
                validEconomics(),
                List.of(11L, 12L),
                null,
                "alice");
    }

    private SimulationLocation validLocation() {
        return SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain", CountryCode.of("ES"));
    }

    private SimulationSystem validSystem() {
        return new SimulationSystem(300.0, 0.81, 0.5, 99.0,
                new SimulationSystem.LossesPct(2.0, 6.0, 1.0, 3.0, 1.0));
    }

    private ConsumptionProfile validDemand() {
        return ConsumptionProfile.of(120000,
                List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                        10000d));
    }

    private SimulationEconomics validEconomics() {
        return new SimulationEconomics(Currency.of("EUR"), 315000.0, 7200.0, 0.18, 0.07, 8, ProjectLifetime.of(20));
    }
}
