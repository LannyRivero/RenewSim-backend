package com.renewsim.backend.scenario_service.infrastructure.persistence.converter;

import com.renewsim.backend.shared.domain.vo.ClimateData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClimateDataJsonConverterTest {

    private final ClimateDataJsonConverter converter = new ClimateDataJsonConverter();

    @Test
    @DisplayName("should serialize and deserialize climate data as JSON")
    void shouldSerializeAndDeserializeClimateDataAsJson() {
        ClimateData climateData = new ClimateData(5.5, 3.2, 22.0);

        String json = converter.convertToDatabaseColumn(climateData);
        ClimateData restored = converter.convertToEntityAttribute(json);

        assertThat(json).contains("avgSolarIrradiation");
        assertThat(restored).isEqualTo(climateData);
    }

    @Test
    @DisplayName("should reject invalid JSON")
    void shouldRejectInvalidJson() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("{not-json}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Could not deserialize ClimateData");
    }
}
