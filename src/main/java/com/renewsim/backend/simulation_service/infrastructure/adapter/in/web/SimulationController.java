package com.renewsim.backend.simulation_service.infrastructure.adapter.in.web;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.simulation_service.application.command.*;
import com.renewsim.backend.simulation_service.application.port.in.*;
import com.renewsim.backend.simulation_service.application.result.*;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyType;
import com.renewsim.backend.simulation_service.dto.SimulationRequestDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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

    private final CreateSimulationUseCase createUseCase;
    private final UpdateSimulationUseCase updateUseCase;
    private final DeleteSimulationUseCase deleteUseCase;
    private final GetSimulationUseCase getUseCase;
    private final GetUserSimulationHistoryUseCase historyUseCase;

    // ==========================================================
    // CREATE SIMULATION
    // ==========================================================
    @Operation(summary = "Create a new simulation")
    @PreAuthorize("hasAuthority('SCOPE_write:simulations')")
    @PostMapping
    public ResponseEntity<SimulationCreationResultDTO> createSimulation(
            @Valid @RequestBody SimulationRequestDTO request,
            Authentication auth) {

        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

        EnergyType energyType = EnergyType.valueOf(request.energyType().toUpperCase());
        ClimateData climateData = new ClimateData(
                request.climate().irradiance(),
                request.climate().wind(),
                request.climate().hydrology());

        CreateSimulationCommand command = new CreateSimulationCommand(
                request.location(),
                energyType,
                request.projectSize(),
                request.budget(),
                climateData,
                List.of(),
                user.username());

        SimulationCreationResultDTO result = createUseCase.createSimulation(command);
        log.info("✅ User {} created simulation {}", auth.getName(), result.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ==========================================================
    // UPDATE SIMULATION
    // ==========================================================
    @Operation(summary = "Update a simulation")
    @PreAuthorize("hasAuthority('SCOPE_write:simulations')")
    @PutMapping("/{id}")
    public ResponseEntity<SimulationUpdateResultDTO> updateSimulation(
            @PathVariable Long id,
            @Valid @RequestBody SimulationRequestDTO request,
            Authentication auth) {

        SimulationQueryResultDTO existing = getUseCase.getSimulationById(new GetSimulationByIdCommand(id));
        if (!isOwner(auth, existing) && !hasAdminRole(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        EnergyType energyType = EnergyType.valueOf(request.energyType().toUpperCase());
        ClimateData climateData = new ClimateData(
                request.climate().irradiance(),
                request.climate().wind(),
                request.climate().hydrology());

        UpdateSimulationCommand command = new UpdateSimulationCommand(
                id,
                request.location(),
                energyType,
                request.projectSize(),
                request.budget(),
                climateData,
                List.of(),
                auth.getName());

        SimulationUpdateResultDTO result = updateUseCase.updateSimulation(command);
        log.info("✏️ User {} updated simulation {}", auth.getName(), id);
        return ResponseEntity.ok(result);
    }

    // ==========================================================
    // GET SIMULATION BY ID
    // ==========================================================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations')")
    public ResponseEntity<SimulationDetailResultDTO> getSimulationById(
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

        return ResponseEntity.ok(result);
    }

    // ==========================================================
    // EXPORT SIMULATION
    // ==========================================================
    @Operation(summary = "Export simulation results as file")
    @PreAuthorize("hasAuthority('SCOPE_export:simulations')")
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
        if (!isOwner(auth, result) && !hasAdminRole(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("📦 User {} exported simulation {}", auth.getName(), id);
        return ResponseEntity.ok("Export successful (placeholder)");
    }

    // ==========================================================
    // DELETE SIMULATION
    // ==========================================================
    @Operation(summary = "Delete simulation by ID")
    @PreAuthorize("hasAuthority('SCOPE_delete:simulations')")
    @DeleteMapping("/{id}")
    public ResponseEntity<SimulationDeletionResultDTO> deleteSimulation(
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
        if (!isOwner(auth, result) && !hasAdminRole(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        SimulationDeletionResultDTO deletion = deleteUseCase.deleteSimulation(new DeleteSimulationCommand(id));
        log.warn("🗑️ User {} deleted simulation {}", auth.getName(), id);

        return ResponseEntity.ok(deletion);
    }

    // ==========================================================
    // GET USER SIMULATION HISTORY
    // ==========================================================
    @Operation(summary = "Get authenticated user simulation history")
    @PreAuthorize("hasAuthority('SCOPE_read:simulations')")
    @GetMapping("/user")
    public ResponseEntity<List<SimulationHistoryResultDTO>> getUserHistory(
            Authentication auth) {

        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

        List<SimulationHistoryResultDTO> history = historyUseCase.getUserHistory(user.username());

        log.info("📜 User {} retrieved {} simulations",
                user.username(), history.size());

        return ResponseEntity.ok(history);
    }

    // ==========================================================
    // Utility methods (Ownership and Roles)
    // ==========================================================
    /**
     * Verifica si el usuario autenticado es el propietario de la simulación.
     */
    private boolean isOwner(Authentication auth, SimulationQueryResultDTO result) {
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
        return result.createdBy().equalsIgnoreCase(user.username());
    }

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
}
