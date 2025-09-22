package com.renewsim.backend.role_service.infrastructure.web.controller;

import com.renewsim.backend.role_service.application.port.in.AssignRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.CreateRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.DeleteRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.GetRolesUseCase;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.dto.RoleDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('roles:write')")
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO request) {
        RoleDTO created = createRoleUseCase.create(request);
        return ResponseEntity.created(URI.create("/api/v1/roles/" + created.id()))
                .body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(getRolesUseCase.getAll());
    }

   
    @GetMapping("/exists/{name}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE_AUTH')")
    public ResponseEntity<Boolean> existsRole(@PathVariable String name) {
        try {
            RoleName roleName = RoleName.valueOf(name.toUpperCase());
            return ResponseEntity.ok(getRolesUseCase.getAll().stream()
                    .anyMatch(r -> r.name().equalsIgnoreCase(roleName.name())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(false);
        }
    }

    
    @PutMapping("/{roleId}/assign/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignRoleToUser(@PathVariable Long roleId,
                                                 @PathVariable Long userId) {
        assignRoleUseCase.assignRoleToUser(roleId, userId);
        return ResponseEntity.noContent().build();
    }

   
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('roles:write')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        deleteRoleUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}

