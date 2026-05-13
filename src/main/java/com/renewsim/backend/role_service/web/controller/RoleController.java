package com.renewsim.backend.role_service.web.controller;

import com.renewsim.backend.role_service.application.command.AssignRoleCommand;
import com.renewsim.backend.role_service.application.command.CreateRoleCommand;
import com.renewsim.backend.role_service.application.command.ManageUserRolesCommand;
import com.renewsim.backend.role_service.application.port.in.*;
import com.renewsim.backend.role_service.application.result.*;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.role_service.web.dto.ManageUserRolesRequestDTO;
import com.renewsim.backend.role_service.web.dto.RoleCreateRequestDTO;
import com.renewsim.backend.role_service.web.dto.RoleDTO;
import com.renewsim.backend.shared.dto.ApiResponseFactory;
import com.renewsim.backend.shared.dto.OperationResponse;
import com.renewsim.backend.shared.exception.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Endpoints for managing system roles")
@RequiredArgsConstructor
public class RoleController {

    private final CreateRoleUseCase createRoleUseCase;
    private final GetRolesUseCase getRolesUseCase;
    private final ExistsRoleUseCase existsRoleUseCase;
    private final AssignRoleUseCase assignRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;
    private final ManageUserRolesUseCase manageUserRolesUseCase;

    // --------------------------
    // POST /roles
    // --------------------------
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_roles:write') or hasRole('ADMIN')")
    @Operation(summary = "Create a new role", description = "Requires role ADMIN or scope roles:write", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "201", description = "Role created successfully", content = @Content(schema = @Schema(implementation = RoleCreationResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "409", description = "Role already exists", content = @Content)
    })
    public ResponseEntity<OperationResponse<RoleCreationResultDTO>> createRole(
            @Valid @RequestBody RoleCreateRequestDTO request) {

        var command = new CreateRoleCommand(request.name(), request.description());
        var result = createRoleUseCase.createRole(command);

        return ResponseEntity
                .status(201)
                .body(ApiResponseFactory.created(result, "Role created successfully"));
    }

    // --------------------------
    // POST /roles/manage
    // --------------------------
    @PostMapping("/manage")
    @PreAuthorize("hasAuthority('SCOPE_roles:write') or hasRole('ADMIN')")
    @Operation(summary = "Batch manage user roles", description = "Assign and revoke multiple roles in a single request", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Roles updated successfully", content = @Content(schema = @Schema(implementation = ManageUserRolesResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "404", description = "User or role not found", content = @Content)
    })
    public ResponseEntity<OperationResponse<ManageUserRolesResultDTO>> manageRoles(
            @Valid @RequestBody ManageUserRolesRequestDTO request) {

        var command = new ManageUserRolesCommand(
                request.requesterId(),
                request.targetUserId(),
                request.rolesToAssign(),
                request.rolesToRevoke());

        var result = manageUserRolesUseCase.manageRoles(command);
        return ResponseEntity.ok(ApiResponseFactory.ok(result, "User roles updated successfully"));
    }

    // --------------------------
    // GET /roles
    // --------------------------
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all roles", description = "Requires ADMIN role", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<OperationResponse<List<RoleDTO>>> getAllRoles() {
        var result = getRolesUseCase.getAll();
        return ResponseEntity.ok(ApiResponseFactory.ok(result, "Roles retrieved successfully"));
    }

    // --------------------------
    // GET /roles/exists/{name}
    // --------------------------
    @GetMapping("/exists/{name}")
    @PreAuthorize("hasAuthority('SCOPE_roles:read') or hasAnyRole('ADMIN','SERVICE_AUTH')")
    @Operation(summary = "Check if role exists", description = "Requires ADMIN, SERVICE_AUTH, or scope roles:read", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<OperationResponse<Boolean>> existsRole(
            @PathVariable @NotBlank String name) {
        boolean exists;
        try {
            exists = existsRoleUseCase.existsByName(RoleName.valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException e) {
            exists = false;
        }
        return ResponseEntity.ok(ApiResponseFactory.ok(exists, "Role existence check completed"));
    }

    // --------------------------
    // GET /roles/by-name/{name}
    // --------------------------

    @GetMapping("/by-name/{name}")
    @PreAuthorize("hasAuthority('SCOPE_roles:read') or hasAnyRole('ADMIN','SERVICE_AUTH')")
    @Operation(summary = "Get role by name", description = "Requires ADMIN, SERVICE_AUTH, or scope roles:read", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<OperationResponse<RoleDTO>> getRoleByName(
            @PathVariable @NotBlank String name) {

        return getRolesUseCase.getAll().stream()
                .filter(r -> r.name().equalsIgnoreCase(name))
                .findFirst()
                .map(role -> ResponseEntity.ok(ApiResponseFactory.ok(role, "Role retrieved successfully")))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name));
    }

    // --------------------------
    // PUT /roles/{roleId}/assign/{userId}
    // --------------------------
    @PutMapping("/{roleId}/assign/{userId}")
    @PreAuthorize("hasAuthority('SCOPE_roles:write') or hasRole('ADMIN')")
    @Operation(summary = "Assign role to user", description = "Requires ADMIN or scope roles:write", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<OperationResponse<RoleAssignmentResultDTO>> assignRoleToUser(
            @PathVariable @NotNull Long roleId,
            @PathVariable @NotNull Long userId) {

        var command = new AssignRoleCommand(null, userId, roleId);
        var result = assignRoleUseCase.assignRoleToUser(command);
        return ResponseEntity.ok(ApiResponseFactory.ok(result, "Role assigned successfully"));
    }

    // --------------------------
    // DELETE /roles/{id}
    // --------------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_roles:delete') or hasRole('ADMIN')")
    @Operation(summary = "Delete a role", description = "Requires ADMIN or scope roles:delete", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<OperationResponse<Void>> deleteRole(@PathVariable @NotNull Long id) {
        deleteRoleUseCase.delete(id);
        return ResponseEntity.ok(ApiResponseFactory.noContent("Role deleted successfully"));
    }
}
