package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.domain.model.SimulationCompletion;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.port.out.SimulationResultSnapshotWriterPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SimulationCompletionAssembler {

    private final SimulationResultSnapshotWriterPort snapshotWriter;

    public SimulationCompletion toCompletion(SimulationDetailsResult result, List<Long> technologyIds) {
        return new SimulationCompletion(
                snapshotWriter.write(result),
                result.technical().annualGenerationKwh(),
                result.financial().annualSavings(),
                result.financial().npv(),
                result.financial().irrPct(),
                result.summary().recommendation(),
                technologyIds);
    }
}