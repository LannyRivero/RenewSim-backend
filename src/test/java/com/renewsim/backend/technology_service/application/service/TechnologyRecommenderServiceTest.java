package com.renewsim.backend.technology_service.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TechnologyRecommenderServiceTest {

    private final TechnologyRecommenderService service = new TechnologyRecommenderService();

    @Test
    @DisplayName("recommendForEnergyType returns expected ids for known energy types")
    void recommendForEnergyTypeReturnsExpectedIds() {
        assertThat(service.recommendForEnergyType("solar")).containsExactly(1L);
        assertThat(service.recommendForEnergyType("WIND")).containsExactly(2L);
        assertThat(service.recommendForEnergyType("hydro")).containsExactly(3L);
    }

    @Test
    @DisplayName("recommendForEnergyType returns empty list for unknown or blank")
    void recommendForEnergyTypeReturnsEmptyListForUnknownOrBlank() {
        assertThat(service.recommendForEnergyType(null)).isEqualTo(List.of());
        assertThat(service.recommendForEnergyType(" ")).isEqualTo(List.of());
        assertThat(service.recommendForEnergyType("NUCLEAR")).isEqualTo(List.of());
    }
}
