package com.renewsim.backend.user_service.web.controller;

import com.renewsim.backend.shared.dto.ApiResponseFactory;
import com.renewsim.backend.shared.dto.OperationResponse;
import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.port.in.ListUsersUseCase;
import com.renewsim.backend.user_service.application.port.in.ExistsUserUseCase;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import com.renewsim.backend.user_service.web.dto.UserFilterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management operations")
public class UserLookupController {

    private final ListUsersUseCase listUsersUseCase;
    private final ExistsUserUseCase existsUserUseCase;

    @Operation(summary = "Get user by username", description = "Requires role ADMIN or scope user:read", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/by-username")
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasRole('ADMIN')")
    public ResponseEntity<OperationResponse<UserResponse>> getByUsername(@RequestParam String username) {
        var filters = new UserFilterRequest(username, null, null);
        var results = listUsersUseCase.listUsers(0, 1, filters);
            if (results.content().isEmpty()) {
            throw new UserNotFoundException("User with username '" + username + "' not found");
            }
        return ResponseEntity
            .ok(ApiResponseFactory.ok(results.content().get(0), "User retrieved successfully"));
    }

    @Operation(summary = "Get user by email", description = "Requires role ADMIN or scope user:read", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/by-email")
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasRole('ADMIN')")
    public ResponseEntity<OperationResponse<UserResponse>> getByEmail(@RequestParam String email) {
        var filters = new UserFilterRequest(null, email, null);
        var results = listUsersUseCase.listUsers(0, 1, filters);
            if (results.content().isEmpty()) {
            throw new UserNotFoundException("User with email '" + email + "' not found");
            }
        return ResponseEntity
            .ok(ApiResponseFactory.ok(results.content().get(0), "User retrieved successfully"));
    }

    @Operation(summary = "Check if user exists", description = "Requires role ADMIN or scope user:read", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/exists")
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasRole('ADMIN') or hasRole('SERVICE_AUTH')")
    public ResponseEntity<OperationResponse<Boolean>> exists(
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String email) {
        boolean exists = existsUserUseCase.existsByUsernameOrEmail(username, email);
        return ResponseEntity.ok(ApiResponseFactory.ok(exists, "User existence check completed"));
    }
}
