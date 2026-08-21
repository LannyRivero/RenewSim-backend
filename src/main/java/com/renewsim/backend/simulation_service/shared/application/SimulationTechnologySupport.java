package com.renewsim.backend.simulation_service.shared.application;

import com.renewsim.backend.simulation_service.create.application.SimulationEngine;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationTechnologyException;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SimulationTechnologySupport {

    private final TechnologyLookupPort technologyLookupPort;
    private final List<SimulationEngine> simulationEngines;

    public ResolvedTechnologyContext resolve(Technology technology, List<Long> requestedTechnologyIds) {
        validateTechnology(technology);
        SimulationEngine simulationEngine = resolveEngine(technology);
        simulationEngine.assertImplemented();
        List<Long> technologyIds = resolveTechnologyIds(technology, requestedTechnologyIds);
        return new ResolvedTechnologyContext(simulationEngine, technologyIds);
    }

    private List<Long> resolveTechnologyIds(Technology technology, List<Long> requestedTechnologyIds) {
        if (requestedTechnologyIds != null && !requestedTechnologyIds.isEmpty()) {
            validateTechnologyIds(technology, requestedTechnologyIds);
            return List.copyOf(requestedTechnologyIds);
        }
        return technologyLookupPort.recommendActiveTechnologyIdsByEnergyType(technology.value());
    }

    private void validateTechnologyIds(Technology technology, List<Long> technologyIds) {
        if (new LinkedHashSet<>(technologyIds).size() != technologyIds.size()) {
            throw new InvalidSimulationTechnologyException(
                    "DUPLICATE_TECHNOLOGY_IDS: technologyIds must not contain duplicates");
        }

        for (Long technologyId : technologyIds) {
            String technologyEnergyType = technologyLookupPort.findActiveEnergyTypeByTechnologyId(technologyId)
                    .orElseThrow(() -> new InvalidSimulationTechnologyException(
                            "UNSUPPORTED_TECHNOLOGY_ID: '" + technologyId
                                    + "' is not registered or is inactive in the technology catalog"));

            if (!technology.value().equals(technologyEnergyType)) {
                throw new InvalidSimulationTechnologyException(
                        "INCOMPATIBLE_TECHNOLOGY_ID: '" + technologyId + "' does not belong to energyType '"
                                + technology.value() + "'");
            }
        }
    }

    private void validateTechnology(Technology technology) {
        if (!technologyLookupPort.existsActiveByEnergyType(technology.value())) {
            throw new InvalidSimulationTechnologyException(
                    "UNSUPPORTED_TECHNOLOGY: '" + technology.value()
                            + "' is not registered or is inactive in the technology catalog");
        }
    }

    private SimulationEngine resolveEngine(Technology technology) {
        return simulationEngines.stream()
                .filter(engine -> engine.supports(technology))
                .findFirst()
                .orElseThrow(() -> new InvalidSimulationTechnologyException(
                        "UNSUPPORTED_TECHNOLOGY: '" + technology.value() + "' is not implemented yet"));
    }

    public record ResolvedTechnologyContext(SimulationEngine engine, List<Long> technologyIds) {
    }
}
