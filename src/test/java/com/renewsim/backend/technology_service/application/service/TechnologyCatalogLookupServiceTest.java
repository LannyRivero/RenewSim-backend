package com.renewsim.backend.technology_service.application.service;

import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.domain.factory.TechnologyFactory;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.EnergyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnologyCatalogLookupServiceTest {

    @Mock
    private TechnologyRepositoryPort technologyRepository;

    @InjectMocks
    private TechnologyCatalogLookupService service;

    @Test
    @DisplayName("existsActiveTechnology delegates to active-id lookup")
    void existsActiveTechnologyDelegatesToActiveIdLookup() {
        when(technologyRepository.findActiveById(5L)).thenReturn(Optional.of(activeSolarTechnology()));

        boolean result = service.existsActiveTechnology(5L);

        assertThat(result).isTrue();
        verify(technologyRepository).findActiveById(5L);
    }

    @Test
    @DisplayName("existsActiveByEnergyType returns false for invalid values")
    void existsActiveByEnergyTypeReturnsFalseForInvalidValues() {
        boolean result = service.existsActiveByEnergyType("unknown");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("findActiveCo2ReductionFactorByEnergyType returns first active factor")
    void findActiveCo2ReductionFactorByEnergyTypeReturnsFirstActiveFactor() {
        when(technologyRepository.findFirstActiveByEnergyType(EnergyType.SOLAR))
                .thenReturn(Optional.of(activeSolarTechnology()));

        Optional<Double> result = service.findActiveCo2ReductionFactorByEnergyType("solar");

        assertThat(result).contains(0.45);
    }

    @Test
    @DisplayName("existsActiveByEnergyType delegates to deterministic first-active lookup")
    void existsActiveByEnergyTypeDelegatesToDeterministicFirstActiveLookup() {
        when(technologyRepository.findFirstActiveByEnergyType(EnergyType.SOLAR))
                .thenReturn(Optional.of(activeSolarTechnology()));

        boolean result = service.existsActiveByEnergyType("solar");

        assertThat(result).isTrue();
        verify(technologyRepository).findFirstActiveByEnergyType(EnergyType.SOLAR);
    }

    private Technology activeSolarTechnology() {
        return TechnologyFactory.create("Solar Utility", 20.0, 1200.0, 2.5, 10.0, 0.45, 25.0, "solar");
    }
}
