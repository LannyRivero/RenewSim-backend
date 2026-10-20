package com.renewsim.backend.user_service.web.controller;

import com.renewsim.backend.shared.dto.ApiResponseFactory;
import com.renewsim.backend.shared.dto.OperationResponse;
import com.renewsim.backend.user_service.application.port.in.UpdateUserRolesUseCase;
import com.renewsim.backend.user_service.application.port.in.AssignUserRoleUseCase;
import com.renewsim.backend.user_service.application.port.in.RemoveUserRoleUseCase;
import com.renewsim.backend.user_service.web.dto.UpdateUserRolesRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management operations")
public class UserRoleController {

    private final UpdateUserRolesUseCase updateUserRolesUseCase;
    private final AssignUserRoleUseCase assignUserRoleUseCase;
    private final RemoveUserRoleUseCase removeUserRoleUseCase;

    @Operation(summary = "Update user roles", description = "Requires role ADMIN", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OperationResponse<Void>> updateUserRoles(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserRolesRequestDTO request) {
        updateUserRolesUseCase.updateUserRoles(id, request);
        return ResponseEntity.ok(ApiResponseFactory.noContent("User roles updated successfully"));
    }

    @Operation(summary = "Assign role to user", description = "Requires role ADMIN", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OperationResponse<Void>> assignUserRole(
        @PathVariable Long userId,
        @PathVariable Long roleId) {
        assignUserRoleUseCase.assignRole(userId, roleId);
        return ResponseEntity.ok(ApiResponseFactory.noContent("Role assigned successfully"));
    }

    @Operation(summary = "Remove role from user", description = "Requires role ADMIN", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OperationResponse<Void>> removeUserRole(
        @PathVariable Long userId,
        @PathVariable Long roleId) {
        removeUserRoleUseCase.removeRole(userId, roleId);
        return ResponseEntity.ok(ApiResponseFactory.noContent("Role removed successfully"));
    }
}
