package com.renewsim.backend.simulation_service.location_lookup.web;

import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;
import com.renewsim.backend.simulation_service.location_lookup.application.port.in.LocationLookupUseCase;
import com.renewsim.backend.simulation_service.location_lookup.web.dto.ResolvedLocationResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulationLocationLookupControllerTest {

    @Mock
    private LocationLookupUseCase locationLookupUseCase;

    @InjectMocks
    private SimulationLocationLookupController controller;

    @Test
    @DisplayName("reverseGeocode returns resolved location from coordinates")
    void reverseGeocodeReturnsResolvedLocation() {
        when(locationLookupUseCase.resolveLocation(-32.8895, -68.8458))
                .thenReturn(new ResolvedLocation("Mendoza", "AR", -32.8895, -68.8458));

        ResolvedLocationResponseDTO response = controller.reverseGeocode(-32.8895, -68.8458).getBody();

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Mendoza");
    }

    @Test
    @DisplayName("searchLocations returns mapped lookup results")
    void searchLocationsReturnsMappedLookupResults() {
        when(locationLookupUseCase.searchLocations("mendoza", 5))
                .thenReturn(List.of(new ResolvedLocation("Mendoza", "AR", -32.8895, -68.8458)));

        List<ResolvedLocationResponseDTO> response = controller.searchLocations("mendoza", 5).getBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().name()).isEqualTo("Mendoza");
    }
}
