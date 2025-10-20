package com.renewsim.backend.simulation_service.infrastructure.adapter.in.web;

import com.renewsim.backend.simulation_service.application.command.*;
import com.renewsim.backend.simulation_service.application.port.in.*;
import com.renewsim.backend.simulation_service.application.result.*;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyType;
import com.renewsim.backend.simulation_service.dto.SimulationRequestDTO;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;

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
 * 🌐 SimulationController
 *
 * ✅ REST endpoints for simulation management with role-based and ownership-based security.
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

    // ==========================================================
    // CREATE SIMULATION
    // ==========================================================
    @Operation(summary = "Create a new simulation")
    @PreAuthorize("hasAuthority('write:simulations')")
    @PostMapping
    public ResponseEntity<SimulationCreationResultDTO> createSimulation(
            @Valid @RequestBody SimulationRequestDTO request,
            Authentication auth) {

        EnergyType energyType = EnergyType.valueOf(request.energyType().toUpperCase());
        ClimateData climateData = new ClimateData(
                request.climate().irradiance(),
                request.climate().wind(),
                request.climate().hydrology()
        );

        CreateSimulationCommand command = new CreateSimulationCommand(
                request.location(),
                energyType,
                request.projectSize(),
                request.budget(),
                climateData,
                List.of()
        );

        SimulationCreationResultDTO result = createUseCase.createSimulation(command);

        log.info("User {} created simulation {}", auth.getName(), result.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ==========================================================
    //  UPDATE SIMULATION
    // ==========================================================
    @Operation(summary = "Update a simulation")
    @PreAuthorize("hasAuthority('write:simulations')")
    @PutMapping("/{id}")
    public ResponseEntity<SimulationUpdateResultDTO> updateSimulation(
            @PathVariable Long id,
            @Valid @RequestBody SimulationRequestDTO request,
            Authentication auth) {

        // TODO: fetch simulation and verify ownership before updating
        log.debug("User {} requested to update simulation {}", auth.getName(), id);

        EnergyType energyType = EnergyType.valueOf(request.energyType().toUpperCase());
        ClimateData climateData = new ClimateData(
                request.climate().irradiance(),
                request.climate().wind(),
                request.climate().hydrology()
        );

        UpdateSimulationCommand command = new UpdateSimulationCommand(
                id,
                request.location(),
                energyType,
                request.projectSize(),
                request.budget(),
                climateData,
                List.of()
        );

        SimulationUpdateResultDTO result = updateUseCase.updateSimulation(command);
        return ResponseEntity.ok(result);
    }

    // ==========================================================
    //  GET SIMULATION BY ID
    // ==========================================================
    @Operation(summary = "Get simulation by ID")
    @PreAuthorize("hasAuthority('read:simulations')")
    @GetMapping("/{id}")
    public ResponseEntity<SimulationQueryResultDTO> getSimulationById(
            @PathVariable Long id,
            Authentication auth) {

        SimulationQueryResultDTO result = getUseCase.getSimulationById(new GetSimulationByIdCommand(id));

        // Ownership check (TODO: replace with actual user ID match)
        if (!isOwner(auth, result.id()) && !hasAdminRole(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(result);
    }

    // ==========================================================
    //  EXPORT SIMULATION
    // ==========================================================
    @Operation(summary = "Export simulation results as file")
    @PreAuthorize("hasAuthority('export:simulations')")
    @GetMapping("/{id}/export")
    public ResponseEntity<String> exportSimulation(@PathVariable Long id, Authentication auth) {
        // TODO: generate PDF/Excel export logic here
        log.info("User {} exported simulation {}", auth.getName(), id);
        return ResponseEntity.ok("Export successful (placeholder)");
    }

    // ==========================================================
    //  DELETE SIMULATION
    // ==========================================================
    @Operation(summary = "Delete simulation by ID")
    @PreAuthorize("hasAuthority('delete:simulations')")
    @DeleteMapping("/{id}")
    public ResponseEntity<SimulationDeletionResultDTO> deleteSimulation(
            @PathVariable Long id,
            Authentication auth) {

        // Ownership check (prevent deleting others’ simulations)
        SimulationQueryResultDTO result = getUseCase.getSimulationById(new GetSimulationByIdCommand(id));
        if (!isOwner(auth, result.id()) && !hasAdminRole(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        SimulationDeletionResultDTO deletion = deleteUseCase.deleteSimulation(new DeleteSimulationCommand(id));
        log.info("User {} deleted simulation {}", auth.getName(), id);

        return ResponseEntity.ok(deletion);
    }

    // ==========================================================
    // Utility methods
    // ==========================================================
    private boolean isOwner(Authentication auth, Long simulationId) {
        // TODO: Compare simulation.userId with auth principal
        return true; // Placeholder until user ownership is implemented
    }

    private boolean hasAdminRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
