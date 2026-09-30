package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.technology_service.application.contract.TechnologyCatalogReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnologyLookupJpaAdapterTest {

    @Mock
    private TechnologyCatalogReader technologyCatalogReader;

    @Test
    @DisplayName("delegates active technology checks by energy type")
    void delegatesActiveTechnologyChecksByEnergyType() {
        when(technologyCatalogReader.existsActiveByEnergyType("solar")).thenReturn(true);

        TechnologyLookupJpaAdapter adapter = new TechnologyLookupJpaAdapter(technologyCatalogReader);

        assertThat(adapter.existsActiveByEnergyType("solar")).isTrue();
    }

    @Test
    @DisplayName("delegates CO2 factor lookup by energy type")
    void delegatesCo2FactorLookupByEnergyType() {
        when(technologyCatalogReader.findActiveCo2ReductionFactorByEnergyType("solar"))
                .thenReturn(Optional.of(0.42));

        TechnologyLookupJpaAdapter adapter = new TechnologyLookupJpaAdapter(technologyCatalogReader);

        assertThat(adapter.findActiveCo2ReductionFactorByEnergyType("solar")).contains(0.42);
    }

    @Test
    @DisplayName("delegates recommended technology ids by energy type")
    void delegatesRecommendedTechnologyIdsByEnergyType() {
        when(technologyCatalogReader.recommendActiveTechnologyIdsByEnergyType("solar"))
                .thenReturn(List.of(1L, 2L));

        TechnologyLookupJpaAdapter adapter = new TechnologyLookupJpaAdapter(technologyCatalogReader);

        assertThat(adapter.recommendActiveTechnologyIdsByEnergyType("solar")).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("delegates active energy type lookup by technology id")
    void delegatesActiveEnergyTypeLookupByTechnologyId() {
        when(technologyCatalogReader.findActiveEnergyTypeByTechnologyId(7L)).thenReturn(Optional.of("solar"));

        TechnologyLookupJpaAdapter adapter = new TechnologyLookupJpaAdapter(technologyCatalogReader);

        assertThat(adapter.findActiveEnergyTypeByTechnologyId(7L)).contains("solar");
    }
}
