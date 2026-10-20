package com.renewsim.backend.user_service.web.controller;

import com.renewsim.backend.shared.dto.ApiResponseFactory;
import com.renewsim.backend.shared.dto.OperationResponse;
import com.renewsim.backend.user_service.application.port.in.CreateUserUseCase;
import com.renewsim.backend.user_service.application.port.in.GetUserUseCase;
import com.renewsim.backend.user_service.application.port.in.ListUsersUseCase;
import com.renewsim.backend.user_service.application.port.in.DeleteUserUseCase;
import com.renewsim.backend.user_service.application.port.in.ActivateUserUseCase;
import com.renewsim.backend.user_service.web.dto.UserCreateRequest;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import com.renewsim.backend.user_service.web.dto.PageResponse;
import com.renewsim.backend.user_service.web.dto.UserFilterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management operations")
public class UserAdministrationController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ActivateUserUseCase activateUserUseCase;

    @Operation(summary = "Get user by ID", description = "Requires role ADMIN, scope user:read or be the owner", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasRole('ADMIN') or @userSec.isOwner(authentication, #id)")
    public ResponseEntity<OperationResponse<UserResponse>> get(@PathVariable Long id) {
        var user = getUserUseCase.getUserById(id);
        return ResponseEntity.ok(ApiResponseFactory.ok(user, "User retrieved successfully"));
    }

    @Operation(summary = "List users", description = "Requires role ADMIN or scope user:read", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasRole('ADMIN')")
    public ResponseEntity<OperationResponse<PageResponse<UserResponse>>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) Boolean enabled) {
        var result = listUsersUseCase.listUsers(page, size, new UserFilterRequest(username, email, enabled));
        return ResponseEntity.ok(ApiResponseFactory.ok(result, "Users listed successfully"));
    }

    @Operation(summary = "Delete user", description = "Requires role ADMIN", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OperationResponse<Void>> delete(@PathVariable Long id) {
        deleteUserUseCase.deleteUser(id);
        return ResponseEntity.ok(ApiResponseFactory.noContent("User deleted successfully"));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('SERVICE_AUTH')")
    public ResponseEntity<OperationResponse<Void>> activate(@PathVariable Long id) {
        activateUserUseCase.activate(id);
        return ResponseEntity.ok(ApiResponseFactory.noContent("User activated successfully"));
    }

    @Operation(summary = "Create user", description = "Requires role ADMIN or scope user:write", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_user:write') or hasRole('ADMIN') or hasRole('SERVICE_AUTH')")
    public ResponseEntity<OperationResponse<UserResponse>> create(@Valid @RequestBody UserCreateRequest req) {
        var created = createUserUseCase.createUser(req);
        return ResponseEntity.status(201)
                .body(ApiResponseFactory.created(created, "User created successfully"));
    }
}
