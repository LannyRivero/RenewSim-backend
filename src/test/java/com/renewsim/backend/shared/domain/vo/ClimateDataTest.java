package com.renewsim.backend.shared.domain.vo;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ClimateDataTest {
    
    @Test
    void shouldCreateClimateData() {
        ClimateData data = new ClimateData(5.5, 12.3, 18.5);
        assertThat(data.avgSolarIrradiation()).isEqualTo(5.5);
        assertThat(data.avgWindSpeed()).isEqualTo(12.3);
        assertThat(data.avgTemperature()).isEqualTo(18.5);
    }
    
    @Test
    void shouldAcceptZeroValues() {
        assertThatCode(() -> new ClimateData(0, 0, 0))
            .doesNotThrowAnyException();
    }
    
    @Test
    void shouldAcceptNegativeTemperature() {
        assertThatCode(() -> new ClimateData(5.5, 12.3, -10.0))
            .doesNotThrowAnyException();
    }
}
