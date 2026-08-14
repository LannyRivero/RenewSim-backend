package com.renewsim.backend.technology_service.application.service;

import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.CapacityFactor;
import com.renewsim.backend.technology_service.domain.model.vo.Co2Reduction;
import com.renewsim.backend.technology_service.domain.model.vo.Efficiency;
import com.renewsim.backend.technology_service.domain.model.vo.EnergyType;
import com.renewsim.backend.technology_service.domain.model.vo.EnvironmentalImpact;
import com.renewsim.backend.technology_service.domain.model.vo.InstallationCost;
import com.renewsim.backend.technology_service.domain.model.vo.MaintenanceCost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Test
    @DisplayName("recommendActiveTechnologyIdsByEnergyType returns active technology ids for the energy type")
    void recommendActiveTechnologyIdsByEnergyTypeReturnsActiveIds() {
        when(technologyRepository.findActiveByEnergyType(EnergyType.SOLAR, PageRequest.of(0, 100)))
                .thenReturn(new PageImpl<>(List.of(activeSolarTechnology(), anotherActiveSolarTechnology())));

        List<Long> result = service.recommendActiveTechnologyIdsByEnergyType("solar");

        assertThat(result).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("recommendActiveTechnologyIdsByEnergyType returns empty list for invalid values")
    void recommendActiveTechnologyIdsByEnergyTypeReturnsEmptyForInvalidValues() {
        List<Long> result = service.recommendActiveTechnologyIdsByEnergyType("unknown");

        assertThat(result).isEmpty();
        verify(technologyRepository, never()).findActiveByEnergyType(any(), any());
    }

    @Test
    @DisplayName("findActiveEnergyTypeByTechnologyId returns the lowercase energy type for active technologies")
    void findActiveEnergyTypeByTechnologyIdReturnsTheLowercaseEnergyTypeForActiveTechnologies() {
        when(technologyRepository.findActiveById(1L)).thenReturn(Optional.of(activeSolarTechnology()));

        Optional<String> result = service.findActiveEnergyTypeByTechnologyId(1L);

        assertThat(result).contains("solar");
        verify(technologyRepository).findActiveById(1L);
    }

    @Test
    @DisplayName("findActiveEnergyTypeByTechnologyId returns empty for unknown technologies")
    void findActiveEnergyTypeByTechnologyIdReturnsEmptyForUnknownTechnologies() {
        when(technologyRepository.findActiveById(99L)).thenReturn(Optional.empty());

        Optional<String> result = service.findActiveEnergyTypeByTechnologyId(99L);

        assertThat(result).isEmpty();
        verify(technologyRepository).findActiveById(99L);
    }

    private Technology activeSolarTechnology() {
        return new Technology(
                1L, "Solar Utility", EnergyType.SOLAR,
                new Efficiency(20.0), new InstallationCost(BigDecimal.valueOf(1200.0)),
                new MaintenanceCost(BigDecimal.valueOf(2.5)),
                new EnvironmentalImpact(10.0), new Co2Reduction(0.45), new CapacityFactor(25.0));
    }

    private Technology anotherActiveSolarTechnology() {
        return new Technology(
                2L, "Solar Farm", EnergyType.SOLAR,
                new Efficiency(22.0), new InstallationCost(BigDecimal.valueOf(1100.0)),
                new MaintenanceCost(BigDecimal.valueOf(2.0)),
                new EnvironmentalImpact(9.0), new Co2Reduction(0.42), new CapacityFactor(24.0));
    }
}
