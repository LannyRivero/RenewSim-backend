package com.renewsim.backend.scenario_service.infrastructure.adapter;

import com.renewsim.backend.technology_service.application.contract.TechnologyCatalogReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnologyLookupJpaAdapterTest {

    @Mock
    private TechnologyCatalogReader technologyCatalogReader;

    @Test
    @DisplayName("existsActiveTechnology delegates to the technology catalog reader")
    void existsActiveTechnologyDelegatesToTechnologyCatalogReader() {
        when(technologyCatalogReader.existsActiveTechnology(7L)).thenReturn(true);

        TechnologyLookupJpaAdapter adapter = new TechnologyLookupJpaAdapter(technologyCatalogReader);

        assertThat(adapter.existsActiveTechnology(7L)).isTrue();
    }
}
