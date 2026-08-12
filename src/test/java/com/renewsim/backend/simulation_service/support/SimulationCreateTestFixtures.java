package com.renewsim.backend.simulation_service.support;

import com.renewsim.backend.simulation_service.create.application.SimulationEngine;
import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.port.out.PvgisSolarResourcePort;
import com.renewsim.backend.simulation_service.create.application.technology.hydro.HydroSimulationEngine;
import com.renewsim.backend.simulation_service.create.application.technology.solar.SolarSimulationAssessmentPolicy;
import com.renewsim.backend.simulation_service.create.application.technology.solar.SolarSimulationEngine;
import com.renewsim.backend.simulation_service.create.application.technology.wind.WindSimulationEngine;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.Currency;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectLifetime;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;

import java.util.List;

public final class SimulationCreateTestFixtures {

        private SimulationCreateTestFixtures() {
        }

        public static CreateRealSimulationCommand validCommand() {
                return new CreateRealSimulationCommand(
                                "Solar - Sevilla",
                                Technology.solar(),
                                SimulationLocation.of("Sevilla, Andalucia, ES", 37.3891, -5.9845, "Spain",
                                                CountryCode.of("ES")),
                                new SimulationSystem(300, 0.81, 0.5, 99, new SimulationSystem.LossesPct(2, 6, 1, 3, 1)),
                                ConsumptionProfile.of(120000,
                                                List.of(10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d, 10000d,
                                                                10000d, 10000d, 10000d, 10000d)),
                                new SimulationEconomics(Currency.of("EUR"), 315000, 7200, 0.18, 0.07, 8,
                                                ProjectLifetime.of(20)),
                                List.of(),
                                null,
                                "alice");
        }

        public static List<SimulationEngine> engines(PvgisSolarResourcePort resourcePort) {
                return List.of(
                                new SolarSimulationEngine(resourcePort, new SolarSimulationAssessmentPolicy()),
                                new WindSimulationEngine(),
                                new HydroSimulationEngine());
        }

        public static PvgisSolarResourcePort.PvgisSolarResourceProfile profile() {
                return new PvgisSolarResourcePort.PvgisSolarResourceProfile(
                                List.of(117.91, 117.78, 140.16, 142.40, 155.64, 154.71, 164.37, 161.96, 145.57, 132.00,
                                                112.14, 112.69),
                                List.of(144.21, 146.57, 177.98, 185.86, 208.58, 212.34, 230.04, 226.42, 197.02, 172.86,
                                                140.05, 137.50),
                                List.of(10.0, 12.0, 15.0, 17.0, 22.0, 27.0, 31.0, 31.0, 27.0, 21.0, 15.0, 11.0),
                                "2005-2020",
                                "PVGIS");
        }
}
