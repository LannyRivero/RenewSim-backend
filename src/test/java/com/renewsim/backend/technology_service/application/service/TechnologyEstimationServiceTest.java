package com.renewsim.backend.technology_service.application.service;

import com.renewsim.backend.shared.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TechnologyEstimationServiceTest {

    private final TechnologyEstimationService service = new TechnologyEstimationService();

    @Test
    @DisplayName("estimate should return defaults for a valid energy type")
    void estimateShouldReturnDefaultsForValidEnergyType() {
        var result = service.estimate("solar", null);

        assertThat(result.energyType()).isEqualTo("SOLAR");
        assertThat(result.suggestedCapacityFactor()).isEqualTo(18.0);
        assertThat(result.confidence()).isEqualTo("medium");
    }

    @Test
    @DisplayName("estimate should reject invalid energy type with bad request")
    void estimateShouldRejectInvalidEnergyTypeWithBadRequest() {
        var exception = assertThrows(BadRequestException.class,
                () -> service.estimate("foo", null));

        assertThat(exception.getMessage()).isEqualTo("Invalid energy type: foo");
    }
}
