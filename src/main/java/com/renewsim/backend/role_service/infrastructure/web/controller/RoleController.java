package com.renewsim.backend.role_service.infrastructure.web.controller;

import com.renewsim.backend.role_service.application.port.in.AssignRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.CreateRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.DeleteRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.GetRolesUseCase;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.dto.RoleCreateRequestDTO;
import com.renewsim.backend.role_service.dto.RoleDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Endpoints for managing system roles")
public class RoleController {

        private final CreateRoleUseCase createRoleUseCase;
        private final GetRolesUseCase getRolesUseCase;
        private final AssignRoleUseCase assignRoleUseCase;
        private final DeleteRoleUseCase deleteRoleUseCase;

        public RoleController(CreateRoleUseCase createRoleUseCase,
                        GetRolesUseCase getRolesUseCase,
                        AssignRoleUseCase assignRoleUseCase,
                        DeleteRoleUseCase deleteRoleUseCase) {
                this.createRoleUseCase = createRoleUseCase;
                this.getRolesUseCase = getRolesUseCase;
                this.assignRoleUseCase = assignRoleUseCase;
                this.deleteRoleUseCase = deleteRoleUseCase;
        }

        // ----------------------
        // POST /roles
        // ----------------------
        @PostMapping
        @PreAuthorize("hasAuthority('SCOPE_roles:write') or hasRole('ADMIN')")
        @Operation(summary = "Create a new role", description = "Requires role **ADMIN** or scope **roles:write**", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "201", description = "Role created successfully", content = @Content(schema = @Schema(implementation = RoleDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                        @ApiResponse(responseCode = "409", description = "Role already exists", content = @Content)
        })
        public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleCreateRequestDTO request) {
                RoleDTO created = createRoleUseCase.create(new RoleDTO(null, request.name()));
                return ResponseEntity.created(URI.create("/api/v1/roles/" + created.id()))
                                .body(created);
        }

        // ----------------------
        // GET /roles
        // ----------------------
        @GetMapping
        @PreAuthorize("hasAuthority('SCOPE_roles:read') or hasAnyRole('ADMIN','USER')")
        @Operation(summary = "List all roles", description = "Requires role **ADMIN**, **USER**, or scope **roles:read**", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "List of roles", content = @Content(schema = @Schema(implementation = RoleDTO.class)))
        })
        public ResponseEntity<List<RoleDTO>> getAllRoles() {
                return ResponseEntity.ok(getRolesUseCase.getAll());
        }

        // ----------------------
        // GET /roles/exists/{name}
        // ----------------------
         @GetMapping("exists/{name}")
        @PreAuthorize("hasAuthority('SCOPE_roles:read') or hasRole('ADMIN') or hasRole('SERVICE_AUTH')")
        @Operation(summary = "Check if role exists", description = "Requires role **ADMIN**, **SERVICE_AUTH**, or scope **roles:read**", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "True if role exists, false otherwise", content = @Content(schema = @Schema(implementation = Boolean.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
        })
        public ResponseEntity<Boolean> existsRole(
                        @Parameter(description = "Role name to check", example = "ADMIN") @PathVariable @NotBlank(message = "Role name cannot be blank") String name) {
                try {
                        RoleName roleName = RoleName.valueOf(name.toUpperCase());
                        return ResponseEntity.ok(getRolesUseCase.getAll().stream()
                                        .anyMatch(r -> r.name().equalsIgnoreCase(roleName.name())));
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.ok(false);
                }
        }

        // ----------------------
        // GET /roles/{name}
        // ----------------------
        @GetMapping("/by-name/{name}")
        @PreAuthorize("hasAuthority('SCOPE_roles:read') or hasRole('ADMIN') or hasRole('SERVICE_AUTH')")
        @Operation(summary = "Get role by name", description = "Requires role **ADMIN**, **SERVICE_AUTH**, or scope **roles:read**", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Role found", content = @Content(schema = @Schema(implementation = RoleDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Role not found", content = @Content)
        })
        public ResponseEntity<RoleDTO> getRoleByName(
                        @Parameter(description = "Role name", example = "USER") @PathVariable @NotBlank String name) {

                try {
                        RoleName roleName = RoleName.valueOf(name.toUpperCase());

                        return getRolesUseCase.getAll().stream()
                                        .filter(r -> r.name().equalsIgnoreCase(roleName.name()))
                                        .findFirst()
                                        .map(ResponseEntity::ok)
                                        .orElse(ResponseEntity.notFound().build());

                } catch (IllegalArgumentException e) {
                        return ResponseEntity.notFound().build();
                }
        }

        // ----------------------
        // PUT /roles/{roleId}/assign/{userId}
        // ----------------------
        @PutMapping("/{roleId}/assign/{userId}")
        @PreAuthorize("hasAuthority('SCOPE_roles:write') or hasRole('ADMIN')")
        @Operation(summary = "Assign role to user", description = "Requires role **ADMIN** or scope **roles:write**", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "204", description = "Role assigned successfully"),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                        @ApiResponse(responseCode = "404", description = "User or role not found", content = @Content)
        })
        public ResponseEntity<Void> assignRoleToUser(
                        @Parameter(description = "Role ID") @PathVariable @NotNull(message = "Role ID cannot be null") Long roleId,
                        @Parameter(description = "User ID") @PathVariable @NotNull(message = "User ID cannot be null") Long userId) {
                assignRoleUseCase.assignRoleToUser(roleId, userId);
                return ResponseEntity.noContent().build();
        }

        // ----------------------
        // DELETE /roles/{id}
        // ----------------------
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('SCOPE_roles:delete') or hasRole('ADMIN')")
        @Operation(summary = "Delete a role", description = "Requires role **ADMIN** or scope **roles:delete**", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Role not found", content = @Content)
        })
        public ResponseEntity<Void> deleteRole(
                        @Parameter(description = "Role ID to delete") @PathVariable @NotNull(message = "Role ID cannot be null") Long id) {
                deleteRoleUseCase.delete(id);
                return ResponseEntity.noContent().build();
        }
}
