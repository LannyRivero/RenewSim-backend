package com.renewsim.backend.simulation_service.location_lookup.application;

import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;
import com.renewsim.backend.simulation_service.location_lookup.application.port.out.LocationLookupProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationLookupServiceTest {

    @Mock
    private LocationLookupProvider locationLookupProvider;

    @InjectMocks
    private LocationLookupService service;

    @Test
    @DisplayName("resolveLocation delegates to provider")
    void resolveLocationDelegatesToProvider() {
        ResolvedLocation expected = new ResolvedLocation("Mendoza", "AR", -32.8895, -68.8458);
        when(locationLookupProvider.resolveLocation(-32.8895, -68.8458)).thenReturn(expected);

        ResolvedLocation result = service.resolveLocation(-32.8895, -68.8458);

        assertThat(result).isEqualTo(expected);
        verify(locationLookupProvider).resolveLocation(-32.8895, -68.8458);
    }

    @Test
    @DisplayName("searchLocations delegates to provider")
    void searchLocationsDelegatesToProvider() {
        List<ResolvedLocation> expected = List.of(new ResolvedLocation("Mendoza", "AR", -32.8895, -68.8458));
        when(locationLookupProvider.searchLocations("mendoza", 5)).thenReturn(expected);

        List<ResolvedLocation> result = service.searchLocations("mendoza", 5);

        assertThat(result).containsExactlyElementsOf(expected);
        verify(locationLookupProvider).searchLocations("mendoza", 5);
    }
}
