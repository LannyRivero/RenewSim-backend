package com.renewsim.backend.simulation_service.web.controller;

import com.renewsim.backend.simulation_service.application.createSimulation.CreateRealSimulationUseCase;
import com.renewsim.backend.simulation_service.application.dashboard.GetPortfolioDashboardUseCase;
import com.renewsim.backend.simulation_service.application.deleteSimulation.DeleteRealSimulationUseCase;
import com.renewsim.backend.simulation_service.application.detailSimulation.GetRealSimulationUseCase;
import com.renewsim.backend.simulation_service.application.historySimulation.ListUserRealSimulationsUseCase;
import com.renewsim.backend.simulation_service.application.port.out.ClimateDataProviderPort;
import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;
import com.renewsim.backend.simulation_service.web.dto.CreateSimulationRequestDTO;
import com.renewsim.backend.simulation_service.web.dto.ListUserSimulationsResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.PortfolioDashboardResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.ResolvedLocationResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationDetailsResponseDTO;
import com.renewsim.backend.shared.domain.vo.Location;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SimulationController
 *
 * REST endpoints for simulation management with role-based and
 * ownership-based security.
 * Implements ownership validation to ensure users can only access their own
 * simulations,
 * unless they have ADMIN privileges.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
@Tag(name = "Simulation API", description = "Operations for renewable energy simulations")
public class SimulationController {

    private final ClimateDataProviderPort climateDataProviderPort;
    private final CreateRealSimulationUseCase createRealSimulationUseCase;
    private final GetRealSimulationUseCase getRealSimulationUseCase;
    private final GetPortfolioDashboardUseCase getPortfolioDashboardUseCase;
    private final ListUserRealSimulationsUseCase listUserRealSimulationsUseCase;
    private final DeleteRealSimulationUseCase deleteRealSimulationUseCase;
    private final SimulationWebMapper webMapper = new SimulationWebMapper();
    private final SimulationRequestContextFactory requestContextFactory = new SimulationRequestContextFactory();

    @Operation(summary = "Create a new simulation")
    @PreAuthorize("hasAuthority('SCOPE_write:simulations') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SimulationDetailsResponseDTO> createSimulation(
            @Valid @RequestBody CreateSimulationRequestDTO request,
            Authentication auth) {

        SimulationRequestContext requestContext = requestContextFactory.from(auth);

        SimulationDetailsResponseDTO result = webMapper.toWebDetails(
                createRealSimulationUseCase.createSimulation(webMapper.toCommand(request, requestContext.username())));
        log.info("User {} created simulation {}", requestContext.username(), result.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    public ResponseEntity<SimulationDetailsResponseDTO> getSimulationById(
            @PathVariable Long id,
            Authentication auth) {
        SimulationRequestContext requestContext = requestContextFactory.from(auth);

        return ResponseEntity.ok(webMapper.toWebDetails(
                getRealSimulationUseCase.getSimulationById(
                        id,
                        requestContext.username(),
                        requestContext.isAdmin())));
    }

    @Operation(summary = "Delete simulation by ID")
    @PreAuthorize("hasAuthority('SCOPE_delete:simulations') or hasRole('ADMIN')")
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteSimulation(
            @PathVariable Long id,
            Authentication auth) {

        SimulationRequestContext requestContext = requestContextFactory.from(auth);

        deleteRealSimulationUseCase.deleteSimulation(id, requestContext.username(), requestContext.isAdmin());
        log.warn("User {} deleted simulation {}", requestContext.username(), id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all simulations of authenticated user")
    @PreAuthorize("hasAuthority('SCOPE_delete:simulations') or hasRole('ADMIN')")
    @DeleteMapping("/user")
    public ResponseEntity<Void> deleteAllUserSimulations(Authentication auth) {

        SimulationRequestContext requestContext = requestContextFactory.from(auth);

        deleteRealSimulationUseCase.deleteAllUserSimulations(requestContext.username());

        log.warn("User {} deleted all simulations", requestContext.username());

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get authenticated user simulations with pagination")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping({ "/user", "/my-simulations" })
    public ResponseEntity<ListUserSimulationsResponseDTO> getMySimulations(Authentication auth) {

        SimulationRequestContext requestContext = requestContextFactory.from(auth);

        ListUserSimulationsResponseDTO history = webMapper.toWebList(
                listUserRealSimulationsUseCase.getUserSimulations(requestContext.username()));
        log.info("User {} retrieved {} simulations", requestContext.username(), history.total());

        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Get executive portfolio dashboard for authenticated user")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<PortfolioDashboardResponseDTO> getDashboard(Authentication auth) {

        SimulationRequestContext requestContext = requestContextFactory.from(auth);

        return ResponseEntity
                .ok(webMapper.toWebDashboard(getPortfolioDashboardUseCase.getDashboard(requestContext.username())));
    }

    @Operation(summary = "Resolve location from coordinates")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping("/locations/reverse")
    public ResponseEntity<ResolvedLocationResponseDTO> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lon) {
        new Location(lat, lon);
        ResolvedLocation resolvedLocation = climateDataProviderPort.resolveLocation(lat, lon);

        return ResponseEntity.ok(new ResolvedLocationResponseDTO(
                resolvedLocation.name(),
                resolvedLocation.country(),
                lat,
                lon));
    }

    @Operation(summary = "Search locations by query")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
    @GetMapping("/locations/search")
    public ResponseEntity<List<ResolvedLocationResponseDTO>> searchLocations(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {
        String query = q == null ? "" : q.trim();
        if (query.length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        int safeLimit = Math.min(Math.max(limit, 1), 10);

        List<ResolvedLocationResponseDTO> results = climateDataProviderPort.searchLocations(query, safeLimit)
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
