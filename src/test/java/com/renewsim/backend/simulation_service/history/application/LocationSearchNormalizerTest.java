package com.renewsim.backend.simulation_service.history.application;

import com.renewsim.backend.simulation_service.shared.domain.service.LocationNameNormalizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationSearchNormalizerTest {

    @Test
    void normalizesDiacriticsCaseAndWhitespace() {
        assertThat(LocationNameNormalizer.normalize("  MÁLAGA   Centro "))
                .isEqualTo("malaga centro");
    }

    @Test
    void preservesNull() {
        assertThat(LocationNameNormalizer.normalize(null)).isNull();
    }
}
