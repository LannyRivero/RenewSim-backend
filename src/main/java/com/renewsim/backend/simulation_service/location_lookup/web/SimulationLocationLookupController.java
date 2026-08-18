package com.renewsim.backend.simulation_service.location_lookup.web;

import com.renewsim.backend.shared.domain.vo.Location;
import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;
import com.renewsim.backend.simulation_service.location_lookup.application.port.in.LocationLookupUseCase;
import com.renewsim.backend.simulation_service.location_lookup.web.dto.ResolvedLocationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Simulation Location Lookup API", description = "Resolve and search geographic locations used as simulation inputs")
public class SimulationLocationLookupController {

    private final LocationLookupUseCase locationLookupUseCase;

    @Operation(summary = "Resolve location from coordinates", description = "Reverse-geocodes latitude/longitude into a human-readable location (city and country) for the simulation form.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Location resolved", content = @Content(schema = @Schema(implementation = ResolvedLocationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Missing read scope", content = @Content)
    })
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping("/reverse")
    public ResponseEntity<ResolvedLocationResponseDTO> reverseGeocode(
            @Parameter(description = "Latitude in decimal degrees", required = true) @RequestParam double lat,
            @Parameter(description = "Longitude in decimal degrees", required = true) @RequestParam double lon) {
        new Location(lat, lon);
        ResolvedLocation resolvedLocation = locationLookupUseCase.resolveLocation(lat, lon);

        return ResponseEntity.ok(new ResolvedLocationResponseDTO(
                resolvedLocation.name(),
                resolvedLocation.country(),
                lat,
                lon));
    }

    @Operation(summary = "Search locations by query", description = "Searches locations by a free-text query (min 2 characters) and returns up to 10 matches to complete the simulation location field.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Locations matched", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResolvedLocationResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Missing read scope", content = @Content)
    })
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<ResolvedLocationResponseDTO>> searchLocations(
            @Parameter(description = "Free-text search query (min 2 characters)", required = true) @RequestParam String q,
            @Parameter(description = "Maximum number of results (clamped to 1-10)", required = false) @RequestParam(defaultValue = "5") int limit) {
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
