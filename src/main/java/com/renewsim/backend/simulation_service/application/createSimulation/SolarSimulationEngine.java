package com.renewsim.backend.simulation_service.application.createSimulation;

import com.renewsim.backend.simulation_service.application.port.out.PvgisSolarResourcePort;
import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.create.application.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.FinancialCalculator;
import com.renewsim.backend.simulation_service.create.application.SimulationEngine;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.renewsim.backend.simulation_service.application.shared.SimulationMathUtils.formatDate;
import static com.renewsim.backend.simulation_service.application.shared.SimulationMathUtils.round;
import static com.renewsim.backend.simulation_service.application.shared.SimulationMathUtils.roundList;
import static com.renewsim.backend.simulation_service.application.shared.SimulationMathUtils.scale;
import static com.renewsim.backend.simulation_service.application.shared.SimulationMathUtils.sum;

@Component
@RequiredArgsConstructor
public class SolarSimulationEngine implements SimulationEngine {

        private static final String MODEL_VERSION = "solar-spain-v1";
        private static final String RESOURCE_SOURCE = "PVGIS";
        private static final String STATUS_COMPLETED = "completed";
        private static final List<String> MONTHS = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
                        "Sep", "Oct", "Nov", "Dec");

        private final PvgisSolarResourcePort resourcePort;
        @Qualifier("solarSimulationAssessmentPolicy")
        private final SimulationAssessmentPolicy assessmentPolicy;

        @Override
        public boolean supports(Technology technology) {
                return "solar".equals(technology.value());
        }

        @Override
        public SimulationDetailsResult simulate(Simulation simulation, CreateRealSimulationCommand command) {
                SolarSystemProfile solarProfile = SolarSystemProfile.from(command.system());
                double totalLossPct = solarProfile.losses().total();
                PvgisSolarResourcePort.PvgisSolarResourceProfile profile = resourcePort.fetchProfile(
                                command.location().lat(), command.location().lng(), totalLossPct);

                return buildResult(simulation, command, solarProfile, profile, totalLossPct);
        }

        private SimulationDetailsResult buildResult(
                        Simulation simulation,
                        CreateRealSimulationCommand command,
                        SolarSystemProfile solarProfile,
                        PvgisSolarResourcePort.PvgisSolarResourceProfile profile,
                        double totalLossPct) {

                ConsumptionProfile demand = simulation.getDemand();

                List<Double> monthlyGeneration = scale(profile.monthlyGenerationPerKwp(),
                                solarProfile.installedCapacityKw() * solarProfile.availabilityPct() / 100.0);
                List<Double> monthlyConsumption = demand.monthlyConsumptionKwh();
                List<SimulationDetailsResult.MonthlyEnergyBalanceItem> balances = new ArrayList<>();

                double annualGeneration = sum(monthlyGeneration);
                double annualConsumption = demand.annualConsumptionKwh();
                double annualSelfConsumed = 0.0;
                double annualExported = 0.0;

                for (int i = 0; i < 12; i++) {
                        double generation = monthlyGeneration.get(i);
                        double consumption = monthlyConsumption.get(i);
                        double selfConsumed = Math.min(generation, consumption);
                        double exported = Math.max(generation - consumption, 0.0);
                        double imported = Math.max(consumption - generation, 0.0);
                        annualSelfConsumed += selfConsumed;
                        annualExported += exported;
                        balances.add(new SimulationDetailsResult.MonthlyEnergyBalanceItem(
                                        MONTHS.get(i), round(generation, 2), round(consumption, 2),
                                        round(selfConsumed, 2), round(exported, 2), round(imported, 2)));
                }

                double specificYield = annualGeneration / solarProfile.installedCapacityKw();
                double capacityFactorPct = annualGeneration / (solarProfile.installedCapacityKw() * 8760.0) * 100.0;
                double selfConsumptionRatePct = annualGeneration <= 0 ? 0.0
                                : annualSelfConsumed / annualGeneration * 100.0;
                double coverageRatePct = annualConsumption <= 0 ? 0.0 : annualSelfConsumed / annualConsumption * 100.0;

                FinancialCalculator.Result financialResult = FinancialCalculator.calculate(command, annualGeneration,
                                annualSelfConsumed, annualExported);
                SimulationAssessmentPolicy.Assessment summaryResult = assessmentPolicy.assess(command, solarProfile,
                                specificYield, financialResult);

                return new SimulationDetailsResult(
                                String.valueOf(simulation.getId()), STATUS_COMPLETED,
                                formatDate(simulation.getCreatedAt()), formatDate(simulation.getUpdatedAt()),
                                MODEL_VERSION, command.technology().value(), buildLocation(command),
                                new SimulationDetailsResult.Summary(summaryResult.recommendation(),
                                                summaryResult.headline(), summaryResult.summary(),
                                                summaryResult.reasons()),
                                toResultInput(command),
                                new SimulationDetailsResult.Technical(
                                                round(annualGeneration, 2), roundList(monthlyGeneration, 2),
                                                round(specificYield, 2), round(solarProfile.performanceRatio(), 4),
                                                round(capacityFactorPct, 2), round(selfConsumptionRatePct, 2),
                                                round(coverageRatePct, 2),
                                                new SimulationDetailsResult.ResourceSeries(RESOURCE_SOURCE,
                                                                profile.climatePeriod(),
                                                                roundList(profile.monthlyIrradianceKwhM2(), 2),
                                                                roundList(profile.monthlyTemperatureC(), 2)),
                                                new SimulationDetailsResult.LossesSummary(
                                                                round(solarProfile.losses().inverter(), 2),
                                                                round(solarProfile.losses().temperature(), 2),
                                                                round(solarProfile.losses().wiring(), 2),
                                                                round(solarProfile.losses().soiling(), 2),
                                                                round(solarProfile.losses().other(), 2),
                                                                round(totalLossPct, 2)),
                                                balances),
                                financialResult.financial(),
                                new SimulationDetailsResult.Assumptions(
                                                round(command.economics().discountRatePct(), 2),
                                                command.economics().projectLifetime().years(),
                                                round(solarProfile.degradationRateAnnualPct(), 2),
                                                round(command.economics().electricityPurchasePricePerKwh(), 4),
                                                round(command.economics().exportPricePerKwh(), 4),
                                                RESOURCE_SOURCE, profile.climatePeriod()),
                                assessmentPolicy.buildWarnings(command, solarProfile));
        }

        private SimulationDetailsResult.ResolvedLocation buildLocation(CreateRealSimulationCommand command) {
                String[] segments = command.location().label().split(",");
                String name = segments.length > 0 ? segments[0].trim() : command.location().label();
                String adminRegion = segments.length > 1 ? segments[1].trim() : null;
                return new SimulationDetailsResult.ResolvedLocation(
                                command.location().label(), name, adminRegion,
                                command.location().country(), command.location().countryCode().value(),
                                command.location().lat(), command.location().lng(),
                                "ES".equalsIgnoreCase(command.location().countryCode().value()) ? "Europe/Madrid"
                                                : null);
        }

        private SimulationDetailsResult.Input toResultInput(CreateRealSimulationCommand command) {
                return new SimulationDetailsResult.Input(
                                command.name(), command.technology().value(),
                                new SimulationDetailsResult.Location(command.location().label(),
                                                command.location().lat(), command.location().lng(),
                                                command.location().country(), command.location().countryCode().value()),
                                new SimulationDetailsResult.SystemSpec(command.system().installedCapacityKw(),
                                                command.system().performanceRatio(),
                                                command.system().degradationRateAnnualPct(),
                                                command.system().availabilityPct(),
                                                new SimulationDetailsResult.LossesPct(
                                                                command.system().lossesPct().inverter(),
                                                                command.system().lossesPct().temperature(),
                                                                command.system().lossesPct().wiring(),
                                                                command.system().lossesPct().soiling(),
                                                                command.system().lossesPct().other())),
                                new SimulationDetailsResult.Demand(command.demand().annualConsumptionKwh(),
                                                command.demand().monthlyConsumptionKwh()),
                                new SimulationDetailsResult.Economics(command.economics().currency().value(),
                                                command.economics().capexTotal(), command.economics().opexAnnual(),
                                                command.economics().electricityPurchasePricePerKwh(),
                                                command.economics().exportPricePerKwh(),
                                                command.economics().discountRatePct(),
                                                command.economics().projectLifetime().years()));
        }
}
