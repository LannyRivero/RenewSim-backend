package com.renewsim.backend.simulation_service.location_lookup.application;

import com.renewsim.backend.shared.config.CacheConfig;
import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;
import com.renewsim.backend.simulation_service.location_lookup.application.port.in.LocationLookupUseCase;
import com.renewsim.backend.simulation_service.location_lookup.application.port.out.LocationLookupProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {CacheConfig.class, LocationLookupCacheTest.TestConfig.class})
@ActiveProfiles("test")
class LocationLookupCacheTest {

    @Configuration
    static class TestConfig {
        @Bean
        LocationLookupProvider locationLookupProvider() {
            return mock(LocationLookupProvider.class);
        }

        @Bean
        LocationLookupUseCase locationLookupUseCase(LocationLookupProvider locationLookupProvider) {
            return new LocationLookupService(locationLookupProvider);
        }
    }

    @Autowired
    private LocationLookupProvider locationLookupProvider;

    @Autowired
    private LocationLookupUseCase locationLookupService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        var cache = cacheManager.getCache("simulationLocationLookup");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("resolveLocation uses cache on second identical call")
    void resolveLocationUsesCacheOnSecondIdenticalCall() {
        ResolvedLocation expected = new ResolvedLocation("Sevilla", "ES", 37.3891, -5.9845);
        when(locationLookupProvider.resolveLocation(37.3891, -5.9845)).thenReturn(expected);

        ResolvedLocation first = locationLookupService.resolveLocation(37.3891, -5.9845);
        ResolvedLocation second = locationLookupService.resolveLocation(37.3891, -5.9845);

        assertThat(first).isEqualTo(expected);
        assertThat(second).isEqualTo(expected);
        verify(locationLookupProvider, times(1)).resolveLocation(37.3891, -5.9845);
    }

    @Test
    @DisplayName("searchLocations uses cache on second identical call")
    void searchLocationsUsesCacheOnSecondIdenticalCall() {
        List<ResolvedLocation> expected = List.of(new ResolvedLocation("Sevilla", "ES", 37.3891, -5.9845));
        when(locationLookupProvider.searchLocations("sevilla", 5)).thenReturn(expected);

        List<ResolvedLocation> first = locationLookupService.searchLocations("sevilla", 5);
        List<ResolvedLocation> second = locationLookupService.searchLocations("sevilla", 5);

        assertThat(first).containsExactlyElementsOf(expected);
        assertThat(second).containsExactlyElementsOf(expected);
        verify(locationLookupProvider, times(1)).searchLocations("sevilla", 5);
    }
}
