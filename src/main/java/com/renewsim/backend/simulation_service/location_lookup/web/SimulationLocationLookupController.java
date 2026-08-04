package com.renewsim.backend.simulation_service.location_lookup.web;

import com.renewsim.backend.shared.domain.vo.Location;
import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;
import com.renewsim.backend.simulation_service.location_lookup.application.port.in.LocationLookupUseCase;
import com.renewsim.backend.simulation_service.location_lookup.web.dto.ResolvedLocationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@ConditionalOnBean(LocationLookupUseCase.class)
@RequestMapping("/api/v1/simulations/locations")
@RequiredArgsConstructor
@Tag(name = "Simulation Location Lookup API", description = "Location resolution and search for simulation inputs")
public class SimulationLocationLookupController {

    private final LocationLookupUseCase locationLookupUseCase;

    @Operation(summary = "Resolve location from coordinates")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping("/reverse")
    public ResponseEntity<ResolvedLocationResponseDTO> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lon) {
        new Location(lat, lon);
        ResolvedLocation resolvedLocation = locationLookupUseCase.resolveLocation(lat, lon);

        return ResponseEntity.ok(new ResolvedLocationResponseDTO(
                resolvedLocation.name(),
                resolvedLocation.country(),
                lat,
                lon));
    }

    @Operation(summary = "Search locations by query")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<ResolvedLocationResponseDTO>> searchLocations(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {
        String query = q == null ? "" : q.trim();
        if (query.length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        int safeLimit = Math.min(Math.max(limit, 1), 10);

        List<ResolvedLocationResponseDTO> results = locationLookupUseCase.searchLocations(query, safeLimit)
                .stream()
                .map(location -> new ResolvedLocationResponseDTO(
                        location.name(),
                        location.country(),
                        location.latitude(),
                        location.longitude()))
                .toList();

        return ResponseEntity.ok(results);
    }
}
