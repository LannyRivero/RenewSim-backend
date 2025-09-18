package com.renewsim.backend.role_service.infrastructure.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.renewsim.backend.role_service.application.port.in.AssignRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.CreateRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.DeleteRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.GetRolesUseCase;
import com.renewsim.backend.role_service.dto.RoleDTO;

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

    // ----------------------
    // POST /roles
    // ----------------------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO request) {
        RoleDTO created = createRoleUseCase.create(request); // ✅ usar create()
        return ResponseEntity.created(URI.create("/api/v1/roles/" + created.id()))
                .body(created);
    }

    // ----------------------
    // GET /roles
    // ----------------------
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(getRolesUseCase.getAll());
    }

    // ----------------------
    // PUT /roles/{roleId}/assign/{userId}
    // ----------------------
    @PutMapping("/{roleId}/assign/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignRoleToUser(@PathVariable Long roleId,
            @PathVariable Long userId) {
        assignRoleUseCase.assignRoleToUser(roleId, userId);
        return ResponseEntity.noContent().build();
    }

    // ----------------------
    // DELETE /roles/{id}
    // ----------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        deleteRoleUseCase.delete(id); // ✅ usar delete()
        return ResponseEntity.noContent().build();
    }
}
