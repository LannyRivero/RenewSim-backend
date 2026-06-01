package com.renewsim.backend.technology_service.web.controller;

import com.renewsim.backend.shared.dto.ApiResponseFactory;
import com.renewsim.backend.shared.dto.OperationResponse;
import com.renewsim.backend.technology_service.application.command.*;
import com.renewsim.backend.technology_service.application.dto.TechnologyEstimateDTO;
import com.renewsim.backend.technology_service.application.port.in.*;
import com.renewsim.backend.technology_service.application.result.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@Slf4j
@RestController
@RequestMapping("/api/v1/technologies")
@RequiredArgsConstructor
@Tag(name = "Technologies", description = "Renewable energy technology catalog management")
public class TechnologyController {

    private final CreateTechnologyUseCase createUseCase;
    private final UpdateTechnologyUseCase updateUseCase;
    private final DeleteTechnologyUseCase deleteUseCase;
    private final GetTechnologyUseCase getUseCase;
    private final EstimateTechnologyUseCase estimateUseCase;

    // ESTIMATE
    @GetMapping("/estimate")
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Estimate technology parameters", description = "Returns estimated capacity factor and annual energy production for a given energy type and optional installed capacity. Useful for pre-filling the creation form.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estimation computed successfully", content = @Content(schema = @Schema(implementation = TechnologyEstimateDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid energy type", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<OperationResponse<TechnologyEstimateDTO>> estimate(
            @Parameter(description = "Energy type (SOLAR, WIND, HYDRO, GEOTHERMAL, BIOMASS)", required = true)
            @RequestParam String energyType,
            @Parameter(description = "Installed capacity in kW (optional)", required = false)
            @RequestParam(required = false) Double installedCapacityKw) {

        var result = estimateUseCase.estimate(energyType, installedCapacityKw);
        return ResponseEntity.ok(ApiResponseFactory.ok(result, "Technology estimation computed successfully"));
    }

    // CREATE
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_admin:write') or hasRole('ADMIN')")
    @Operation(summary = "Create new technology", description = "Creates a new renewable energy technology in the catalog. Requires ADMIN role or scope admin:write.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Technology created successfully", content = @Content(schema = @Schema(implementation = TechnologyCreationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - validation error or duplicate technology name", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions", content = @Content)
    })
    public ResponseEntity<OperationResponse<TechnologyCreationResultDTO>> create(
            @Valid @RequestBody CreateTechnologyCommand command) {

        var result = createUseCase.createTechnology(command);
        return ResponseEntity
                .status(201)
                .body(ApiResponseFactory.created(result, "Technology created successfully"));
    }

    // GET by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get technology by ID", description = "Retrieves detailed information about a specific renewable energy technology", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Technology found", content = @Content(schema = @Schema(implementation = TechnologyResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Technology not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<OperationResponse<TechnologyResponseDTO>> getById(
            @Parameter(description = "Technology unique identifier", required = true) @PathVariable Long id) {
        var result = getUseCase.getTechnologyById(new GetTechnologyByIdCommand(id));
        return ResponseEntity.ok(ApiResponseFactory.ok(result, "Technology retrieved successfully"));
    }

    // GET ALL
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasAnyRole('USER','ADMIN')")
    @Operation(summary = "List all technologies", description = "Retrieves all available renewable energy technologies with their technical specifications", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Technologies retrieved successfully", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content)
    })
    public ResponseEntity<OperationResponse<Page<TechnologyResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String energyType) {
        var results = getUseCase.getTechnologies(page, size, energyType);
        return ResponseEntity.ok(ApiResponseFactory.ok(results, "All technologies retrieved"));
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin:write') or hasRole('ADMIN')")
    @Operation(summary = "Update technology", description = "Updates an existing renewable energy technology. Requires ADMIN role or scope admin:write.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Technology updated successfully", content = @Content(schema = @Schema(implementation = TechnologyUpdateResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - validation error", content = @Content),
            @ApiResponse(responseCode = "404", description = "Technology not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions", content = @Content)
    })
    public ResponseEntity<OperationResponse<TechnologyUpdateResultDTO>> update(
            @Parameter(description = "Technology unique identifier", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateTechnologyCommand command) {

        // Crear un nuevo record con el id del path
        var commandWithId = UpdateTechnologyCommand.withId(id, command);
        var result = updateUseCase.updateTechnology(commandWithId);

        return ResponseEntity.ok(ApiResponseFactory.ok(result, "Technology updated successfully"));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin:delete') or hasRole('ADMIN')")
    @Operation(summary = "Delete technology", description = "Permanently removes a technology from the catalog. Requires ADMIN role or scope admin:delete.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Technology deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Technology not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions", content = @Content)
    })
    public ResponseEntity<OperationResponse<Void>> delete(
            @Parameter(description = "Technology unique identifier", required = true) @PathVariable Long id) {
        deleteUseCase.deleteTechnology(new DeleteTechnologyCommand(id));
        return ResponseEntity.ok(ApiResponseFactory.noContent("Technology deleted successfully"));
    }
}
