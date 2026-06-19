package com.renewsim.backend.simulation_service.web.controller;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.simulation_service.application.command.*;
import com.renewsim.backend.simulation_service.application.port.in.*;
import com.renewsim.backend.simulation_service.application.result.*;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyType;
import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;
import com.renewsim.backend.simulation_service.application.port.out.ClimateDataProviderPort;
import com.renewsim.backend.simulation_service.web.mapper.SimulationResponseMapper;
import com.renewsim.backend.simulation_service.web.dto.SimulationRequestDTO;
import com.renewsim.backend.simulation_service.web.dto.*;
import com.renewsim.backend.shared.domain.vo.Location;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import com.renewsim.backend.user_service.web.dto.PageResponse;

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

        private final CreateSimulationUseCase createUseCase;
        private final UpdateSimulationUseCase updateUseCase;
        private final DeleteSimulationUseCase deleteUseCase;
        private final GetSimulationUseCase getUseCase;
        private final GetUserSimulationHistoryUseCase historyUseCase;
        private final GetSimulationDashboardSummaryUseCase dashboardSummaryUseCase;
        private final DeleteAllSimulationsByUserUseCase deleteAllSimulationsByUserUseCase;
        private final ClimateDataProviderPort climateDataProviderPort;
        private final SimulationResponseMapper responseMapper;

        // ==========================================================
        // CREATE SIMULATION
        // ==========================================================
        @Operation(summary = "Create a new simulation")
        @PreAuthorize("hasAuthority('SCOPE_write:simulations') or hasRole('ADMIN')")
        @PostMapping
        public ResponseEntity<CreateSimulationResponseDTO> createSimulation(
                        @Valid @RequestBody SimulationRequestDTO request,
                        Authentication auth) {

                AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

                EnergyType energyType = EnergyType.fromString(request.technology());

                CreateSimulationCommand command = new CreateSimulationCommand(
                                request.name(),
                                formatLocationName(request.location().lat(), request.location().lon()),
                                request.location().lat(),
                                request.location().lon(),
                                energyType,
                                request.installedCapacity(),
                                0,
                                null,
                                List.of(),
                                user.username());

                SimulationCreationResultDTO result = createUseCase.createSimulation(command);
                log.info("✅ User {} created simulation {}", user.username(), result.id());
                return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toCreateResponse(result));
        }

        // ==========================================================
        // UPDATE SIMULATION
        // ==========================================================
        @Operation(summary = "Update a simulation")
        @PreAuthorize("hasAuthority('SCOPE_write:simulations') or hasRole('ADMIN')")
        @PutMapping("/{id}")
        public ResponseEntity<SimulationUpdateResultDTO> updateSimulation(
                        @PathVariable Long id,
                        @Valid @RequestBody SimulationRequestDTO request,
                        Authentication auth) {

                AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
                boolean isAdmin = hasAdminRole(auth);
                SimulationDetailResultDTO existing = getUseCase.getSimulationById(
                                new GetSimulationByIdCommand(id, user.username(), isAdmin));
                if (!isOwner(auth, existing) && !hasAdminRole(auth)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }

                EnergyType energyType = EnergyType.fromString(request.technology());

                UpdateSimulationCommand command = new UpdateSimulationCommand(
                                id,
                                request.name(),
                                formatLocationName(request.location().lat(), request.location().lon()),
                                request.location().lat(),
                                request.location().lon(),
                                energyType,
                                request.installedCapacity(),
                                0,
                                null,
                                List.of(),
                                user.username());

                SimulationUpdateResultDTO result = updateUseCase.updateSimulation(command);
                log.info("✏️ User {} updated simulation {}", user.username(), id);
                return ResponseEntity.ok(result);
        }

        // ==========================================================
        // GET SIMULATION BY ID
        // ==========================================================
        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
        public ResponseEntity<SimulationResultsResponseDTO> getSimulationById(
                        @PathVariable Long id,
                        Authentication auth) {
                AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

                boolean isAdmin = auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                SimulationDetailResultDTO result = getUseCase.getSimulationById(
                                new GetSimulationByIdCommand(
                                                id,
                                                user.username(),
                                                isAdmin));

                return ResponseEntity.ok(responseMapper.toResultsResponse(result));
        }

        // ==========================================================
        // EXPORT SIMULATION
        // ==========================================================
        @Operation(summary = "Export simulation results as file")
        @PreAuthorize("hasAuthority('SCOPE_export:simulations') or hasRole('ADMIN')")
        @GetMapping("/{id}/export")
        public ResponseEntity<String> exportSimulation(
                        @PathVariable Long id,
                        Authentication auth) {

                AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

                boolean isAdmin = auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                SimulationDetailResultDTO result = getUseCase.getSimulationById(
                                new GetSimulationByIdCommand(
                                                id,
                                                user.username(),
                                                isAdmin));
                log.info("📦 User {} exported simulation {}", user.username(), id);
                return ResponseEntity.ok("Export successful (placeholder)");
        }

        // ==========================================================
        // DELETE SIMULATION
        // ==========================================================
        @Operation(summary = "Delete simulation by ID")
        @PreAuthorize("hasAuthority('SCOPE_delete:simulations') or hasRole('ADMIN')")
        @DeleteMapping("/{id}")
        public ResponseEntity<SimulationDeletionResultDTO> deleteSimulation(
                        @PathVariable Long id,
                        Authentication auth) {

                AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

                boolean isAdmin = auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                getUseCase.getSimulationById(new GetSimulationByIdCommand(id, user.username(), isAdmin));

                SimulationDeletionResultDTO deletion = deleteUseCase.deleteSimulation(new DeleteSimulationCommand(id));
                log.warn("🗑️ User {} deleted simulation {}", user.username(), id);

                return ResponseEntity.ok(deletion);
        }

        @Operation(summary = "Delete all simulations of authenticated user")
        @PreAuthorize("hasAuthority('SCOPE_delete:simulations') or hasRole('ADMIN')")
        @DeleteMapping("/user")
        public ResponseEntity<Void> deleteAllUserSimulations(Authentication auth) {

                AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

                deleteAllSimulationsByUserUseCase
                                .deleteAllByUser(new DeleteAllSimulationsByUserCommand(user.username()));

                log.warn("🧹 User {} deleted ALL simulations", user.username());

                return ResponseEntity.noContent().build();
        }

        // ==========================================================
        // GET USER SIMULATION HISTORY
        // ==========================================================
        @Operation(summary = "Get authenticated user simulations with pagination")
        @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
        @GetMapping("/my-simulations")
        public ResponseEntity<PageResponse<UserSimulationSummaryDTO>> getMySimulations(
                        Authentication auth,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(required = false) String status) {

                AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

                List<UserSimulationSummaryDTO> history = historyUseCase.getUserHistory(user.username())
                                .stream()
                                .map(responseMapper::toUserSummary)
                                .filter(simulation -> matchesStatus(simulation, status))
                                .toList();

                int safePage = Math.max(page, 0);
                int safeSize = Math.max(size, 1);
                int start = Math.min(safePage * safeSize, history.size());
                int end = Math.min(start + safeSize, history.size());

                PageImpl<UserSimulationSummaryDTO> historyPage = new PageImpl<>(
                                history.subList(start, end),
                                org.springframework.data.domain.PageRequest.of(safePage, safeSize),
                                history.size());

                log.info("📄 User {} retrieved page {} of simulations with {} items",
                                user.username(), safePage, historyPage.getNumberOfElements());

                return ResponseEntity.ok(new PageResponse<>(
                                historyPage.getContent(),
                                historyPage.getNumber(),
                                historyPage.getSize(),
                                historyPage.getTotalElements(),
                                historyPage.getTotalPages(),
                                historyPage.isLast()));
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

        @Operation(summary = "Get authenticated user dashboard summary")
        @PreAuthorize("hasAuthority('SCOPE_read:simulations') or hasRole('ADMIN')")
        @GetMapping("/dashboard")
        public ResponseEntity<DashboardSummaryResponseDTO> getDashboardSummary(Authentication auth) {
                AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

                SimulationDashboardSummaryResult result = dashboardSummaryUseCase.getDashboardSummary(user.username());

                return ResponseEntity.ok(new DashboardSummaryResponseDTO(
                                new DashboardStatsResponseDTO(
                                                result.stats().totalSimulations(),
                                                result.stats().totalEnergyGeneratedKwh(),
                                                result.stats().totalCo2SavedKg(),
                                                result.stats().averageRoiPercent()),
                                result.energyBySource().stream()
                                                .map(item -> new DashboardEnergyBySourceResponseDTO(item.label(), item.kwh()))
                                                .toList(),
                                result.efficiencyMetrics().stream()
                                                .map(item -> new DashboardEfficiencyMetricResponseDTO(item.label(), item.value(), item.hint()))
                                                .toList(),
                                result.targetVsActual().stream()
                                                .map(item -> new DashboardTargetVsActualResponseDTO(item.label(), item.actual(), item.target(), item.unit()))
                                                .toList()));
        }

        // ==========================================================
        // Utility methods (Ownership and Roles)
        // ==========================================================
        /**
         * Verifica si el usuario autenticado es el propietario de la simulación.
         */
        private boolean isOwner(Authentication auth, SimulationDetailResultDTO result) {
                AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
                return result.createdBy().equalsIgnoreCase(user.username());
        }

        /**
         * Comprueba si el usuario autenticado tiene el rol ADMIN.
         */
        private boolean hasAdminRole(Authentication auth) {
                return auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        private boolean matchesStatus(UserSimulationSummaryDTO simulation, String status) {
                return status == null || simulation.status().equalsIgnoreCase(status);
        }

        private String formatLocationName(double latitude, double longitude) {
                return String.format("%.4f, %.4f", latitude, longitude);
        }
}
