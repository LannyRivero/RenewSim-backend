package com.renewsim.backend.simulation_service.create.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.domain.model.SimulationCompletion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimulationCompletionMapper {

    private final ObjectMapper objectMapper;

    SimulationCompletion toCompletion(SimulationDetailsResult result) {
        return new SimulationCompletion(
                writeJson(result),
                result.technical().annualGenerationKwh(),
                result.financial().annualSavings(),
                result.financial().npv(),
                result.financial().irrPct(),
                result.summary().recommendation());
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize simulation payload", ex);
        }
    }
}
