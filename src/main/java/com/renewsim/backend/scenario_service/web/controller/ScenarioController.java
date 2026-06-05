package com.renewsim.backend.scenario_service.web.controller;

import com.renewsim.backend.scenario_service.application.command.CreateScenarioCommand;
import com.renewsim.backend.scenario_service.application.command.GetScenarioByIdCommand;
import com.renewsim.backend.scenario_service.application.command.UpdateScenarioCommand;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultCapacityKw;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultConsumption;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultTariff;
import com.renewsim.backend.scenario_service.domain.model.vo.ScenarioTechnologyId;
import com.renewsim.backend.scenario_service.application.port.in.CreateScenarioUseCase;
import com.renewsim.backend.scenario_service.application.port.in.GetScenarioUseCase;
import com.renewsim.backend.scenario_service.application.port.in.UpdateScenarioUseCase;
import com.renewsim.backend.scenario_service.application.result.ScenarioResponseDTO;
import com.renewsim.backend.scenario_service.web.dto.ScenarioRequestDTO;
import com.renewsim.backend.shared.domain.vo.Money;
import com.renewsim.backend.shared.dto.ApiResponseFactory;
import com.renewsim.backend.shared.dto.OperationResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scenarios")
@RequiredArgsConstructor
@Tag(name = "Scenarios", description = "Predefined scenario catalog management")
public class ScenarioController {

    private final GetScenarioUseCase getScenarioUseCase;
    private final CreateScenarioUseCase createScenarioUseCase;
    private final UpdateScenarioUseCase updateScenarioUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasAnyRole('USER','ADMIN')")
    @Operation(summary = "List active scenarios", description = "Returns all active predefined scenarios", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scenarios retrieved successfully", content = @Content(schema = @Schema(implementation = ScenarioResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<OperationResponse<List<ScenarioResponseDTO>>> getAll() {
        var result = getScenarioUseCase.getAllActiveScenarios();
        return ResponseEntity.ok(ApiResponseFactory.ok(result, "Scenarios retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get scenario by ID", description = "Returns the detail of a predefined scenario", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scenario found", content = @Content(schema = @Schema(implementation = ScenarioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Scenario not found", content = @Content)
    })
    public ResponseEntity<OperationResponse<ScenarioResponseDTO>> getById(
            @Parameter(description = "Scenario unique identifier", required = true) @PathVariable Long id) {
        var result = getScenarioUseCase.getScenarioById(new GetScenarioByIdCommand(id));
        return ResponseEntity.ok(ApiResponseFactory.ok(result, "Scenario retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_admin:write') or hasRole('ADMIN')")
    @Operation(summary = "Create scenario", description = "Creates a new predefined scenario", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Scenario created successfully", content = @Content(schema = @Schema(implementation = ScenarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    public ResponseEntity<OperationResponse<ScenarioResponseDTO>> create(@Valid @RequestBody ScenarioRequestDTO request) {
        var result = createScenarioUseCase.createScenario(toCreateCommand(request));
        return ResponseEntity.status(201).body(ApiResponseFactory.created(result, "Scenario created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin:write') or hasRole('ADMIN')")
    @Operation(summary = "Update scenario", description = "Updates an existing predefined scenario", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scenario updated successfully", content = @Content(schema = @Schema(implementation = ScenarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Scenario not found", content = @Content)
    })
    public ResponseEntity<OperationResponse<ScenarioResponseDTO>> update(
            @Parameter(description = "Scenario unique identifier", required = true) @PathVariable Long id,
            @Valid @RequestBody ScenarioRequestDTO request) {
        var result = updateScenarioUseCase.updateScenario(toUpdateCommand(id, request));
        return ResponseEntity.ok(ApiResponseFactory.ok(result, "Scenario updated successfully"));
    }

    private CreateScenarioCommand toCreateCommand(ScenarioRequestDTO request) {
        return new CreateScenarioCommand(
                request.name(),
                request.description(),
                new ScenarioTechnologyId(request.technologyId()),
                new DefaultCapacityKw(request.defaultCapacityKw()),
                new Money(request.defaultInvestmentAmount(), request.defaultInvestmentCurrency()),
                new DefaultTariff(request.defaultTariff()),
                new DefaultConsumption(request.defaultConsumption()),
                request.climateProfile());
    }

    private UpdateScenarioCommand toUpdateCommand(Long id, ScenarioRequestDTO request) {
        return new UpdateScenarioCommand(
                id,
                request.name(),
                request.description(),
                new ScenarioTechnologyId(request.technologyId()),
                new DefaultCapacityKw(request.defaultCapacityKw()),
                new Money(request.defaultInvestmentAmount(), request.defaultInvestmentCurrency()),
                new DefaultTariff(request.defaultTariff()),
                new DefaultConsumption(request.defaultConsumption()),
                request.climateProfile());
    }
}
