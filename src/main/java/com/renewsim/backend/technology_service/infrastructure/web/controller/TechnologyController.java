package com.renewsim.backend.technology_service.infrastructure.web.controller;

import com.renewsim.backend.shared.dto.ApiResponseFactory;
import com.renewsim.backend.shared.dto.OperationResponse;
import com.renewsim.backend.technology_service.application.command.*;
import com.renewsim.backend.technology_service.application.port.in.*;
import com.renewsim.backend.technology_service.application.result.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/technologies")
@RequiredArgsConstructor
public class TechnologyController {

    private final CreateTechnologyUseCase createUseCase;
    private final UpdateTechnologyUseCase updateUseCase;
    private final DeleteTechnologyUseCase deleteUseCase;
    private final GetTechnologyUseCase getUseCase;

    // CREATE
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_admin:write') or hasRole('ADMIN')")
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
    public ResponseEntity<OperationResponse<TechnologyQueryResultDTO>> getById(@PathVariable Long id) {
        var result = getUseCase.getTechnologyById(new GetTechnologyByIdCommand(id));
        return ResponseEntity.ok(ApiResponseFactory.ok(result, "Technology retrieved successfully"));
    }

    // GET ALL
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasAnyRole('USER','ADMIN')")
    public ResponseEntity<OperationResponse<List<TechnologyQueryResultDTO>>> getAll() {
        var results = getUseCase.getAllTechnologies();
        return ResponseEntity.ok(ApiResponseFactory.ok(results, "All technologies retrieved"));
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin:write') or hasRole('ADMIN')")
    public ResponseEntity<OperationResponse<TechnologyUpdateResultDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTechnologyCommand command) {

        // Crear un nuevo record con el id del path
        var commandWithId = UpdateTechnologyCommand.withId(id, command);
        var result = updateUseCase.updateTechnology(commandWithId);

        return ResponseEntity.ok(ApiResponseFactory.ok(result, "Technology updated successfully"));
    }

    //  DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin:delete') or hasRole('ADMIN')")
    public ResponseEntity<OperationResponse<Void>> delete(@PathVariable Long id) {
        deleteUseCase.deleteTechnology(new DeleteTechnologyCommand(id));
        return ResponseEntity.ok(ApiResponseFactory.noContent("Technology deleted successfully"));
    }
}
