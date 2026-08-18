package com.renewsim.backend.simulation_service.update.web;

import com.renewsim.backend.simulation_service.create.web.dto.CreateSimulationRequestDTO;
import com.renewsim.backend.simulation_service.create.web.dto.SimulationLocationRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateSimulationWebMapperTest {

        private final UpdateSimulationWebMapper mapper = new UpdateSimulationWebMapper();

        @Test
        @DisplayName("toCommand maps id and request DTO into application command")
        void toCommandMapsIdAndRequestDtoIntoApplicationCommand() {
                CreateSimulationRequestDTO request = request();

                var command = mapper.toCommand(55L, request);

                assertThat(command.simulationId()).isEqualTo(55L);
                assertThat(command.name()).isEqualTo("Solar - Sevilla");
                assertThat(command.technology().value()).isEqualTo("solar");
                assertThat(command.location().country()).isEqualTo("Spain");
                assertThat(command.system().installedCapacityKw()).isEqualTo(300.0);
                assertThat(command.demand().annualConsumptionKwh()).isEqualTo(120000.0);
                assertThat(command.economics().currency().value()).isEqualTo("EUR");
                assertThat(command.technologyIds()).containsExactly(11L, 12L);
        }

        @Test
        @DisplayName("toCommand defaults null technologyIds to an empty list")
        void toCommandDefaultsNullTechnologyIdsToEmptyList() {
                CreateSimulationRequestDTO request = new CreateSimulationRequestDTO(
                                "Solar - Sevilla",
                                "solar",
                                new SimulationLocationRequestDTO("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain",
                                                "ES"),
                                new CreateSimulationRequestDTO.SystemDTO(300, 0.81, 0.5, 99,
                                                new CreateSimulationRequestDTO.LossesPctDTO(2, 6, 1, 3, 1)),
                                new CreateSimulationRequestDTO.DemandDTO(120000,
                                                List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                                                10000d, 10000d, 10000d,
                                                                10000d)),
                                new CreateSimulationRequestDTO.EconomicsDTO("EUR", 315000, 7200, 0.18, 0.07, 8, 20),
                                null);

                var command = mapper.toCommand(55L, request);

                assertThat(command.technologyIds()).isEmpty();
        }

        private CreateSimulationRequestDTO request() {
                return new CreateSimulationRequestDTO(
                                "Solar - Sevilla",
                                "solar",
                                new SimulationLocationRequestDTO("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain",
                                                "ES"),
                                new CreateSimulationRequestDTO.SystemDTO(300, 0.81, 0.5, 99,
                                                new CreateSimulationRequestDTO.LossesPctDTO(2, 6, 1, 3, 1)),
                                new CreateSimulationRequestDTO.DemandDTO(120000,
                                                List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                                                10000d, 10000d, 10000d,
                                                                10000d)),
                                new CreateSimulationRequestDTO.EconomicsDTO("EUR", 315000, 7200, 0.18, 0.07, 8, 20),
                                List.of(11L, 12L));
        }
}
