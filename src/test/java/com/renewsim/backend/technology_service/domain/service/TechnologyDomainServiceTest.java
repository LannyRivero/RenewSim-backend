package com.renewsim.backend.technology_service.domain.service;

import com.renewsim.backend.technology_service.domain.model.Technology;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TechnologyDomainServiceTest {

    private final TechnologyDomainService service = new TechnologyDomainService();

    @Test
    void shouldNormalizeCorrectly() {
        assertEquals(0.5, service.normalize(50, 0, 100));
        assertEquals(0.0, service.normalize(10, 10, 10)); // edge case
    }

    @Test
    void shouldFindMostEfficientTechnology() {
        var solar = new Technology("Solar", 0.8, 1000, 100, 10, 200, 6000, "SOLAR");
        var wind = new Technology("Wind", 0.7, 800, 80, 12, 300, 7000, "WIND");

        var result = service.findMostEfficient(List.of(solar, wind));
        assertTrue(result.isPresent());
        assertEquals("Wind", result.get().name());
    }

    @Test
    void shouldCalculateScoreCorrectly() {
        var tech = new Technology("Hydro", 0.9, 2000, 150, 5, 400, 12000, "HYDRO");
        double score = service.calculateScore(tech);
        assertTrue(score > 0);
    }

    @Test
    void shouldFindBestOverallTechnology() {
        var solar = new Technology("Solar", 0.8, 1000, 100, 15, 200, 6000, "SOLAR");
        var wind = new Technology("Wind", 0.7, 800, 80, 10, 300, 7000, "WIND");

        var result = service.findBestOverall(List.of(solar, wind));
        assertTrue(result.isPresent());
        assertEquals("Solar", result.get().name());
    }
}
