package com.renewsim.backend.user_service.web.controller;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.shared.dto.ApiResponseFactory;
import com.renewsim.backend.shared.dto.OperationResponse;
import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.command.ChangeMyPasswordCommand;
import com.renewsim.backend.user_service.application.command.UpdateMyProfileCommand;
import com.renewsim.backend.user_service.application.port.in.*;
import com.renewsim.backend.user_service.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management operations")
public class UserController {

        private final CreateUserUseCase createUserUseCase;
        private final ExistsUserUseCase existsUserUseCase;
        private final GetUserUseCase getUserUseCase;
        private final ListUsersUseCase listUsersUseCase;
        private final UpdateUserRolesUseCase updateUserRolesUseCase;
        private final GetMyProfileUseCase getMyProfileUseCase;
        private final UpdateMyProfileUseCase updateMyProfileUseCase;
        private final ChangeMyPasswordUseCase changeMyPasswordUseCase;
        private final ActivateUserUseCase activateUserUseCase;

        // ----------------------------------------------------
        // POST /users → Create
        // ----------------------------------------------------
        @Operation(summary = "Create user", description = "Requires role ADMIN or scope user:write", security = @SecurityRequirement(name = "bearerAuth"))
        @PostMapping
        @PreAuthorize("hasAuthority('SCOPE_user:write') or hasRole('ADMIN') or hasRole('SERVICE_AUTH')")
        public ResponseEntity<OperationResponse<UserResponse>> create(@Valid @RequestBody UserCreateRequest req) {
                var created = createUserUseCase.createUser(req);
                return ResponseEntity.status(201)
                                .body(ApiResponseFactory.created(created, "User created successfully"));
        }

        // ----------------------------------------------------
        // GET /users/me → Get own profile
        // ----------------------------------------------------
        @Operation(summary = "Get my profile", description = "Returns the authenticated user's profile", security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/me")
        public ResponseEntity<OperationResponse<UserResponse>> getMe(
                        @AuthenticationPrincipal AuthenticatedUser principal) {
                var response = getMyProfileUseCase.getMyProfileByEmail(principal.username());
                return ResponseEntity.ok(ApiResponseFactory.ok(response, "Profile retrieved successfully"));
        }

        // ----------------------------------------------------
        // PUT /users/me → Update own profile
        // ----------------------------------------------------
        @Operation(summary = "Update my profile", description = "Updates fullName and phone", security = @SecurityRequirement(name = "bearerAuth"))
        @PutMapping("/me")
        public ResponseEntity<OperationResponse<UserResponse>> updateMe(
                        @AuthenticationPrincipal AuthenticatedUser principal,
                        @Valid @RequestBody UpdateMyProfileRequestDTO request) {
                var user = getUserUseCase.getDomainUserByUsernameOrEmail(null, principal.username());
                var response = updateMyProfileUseCase.updateMyProfile(
                                new UpdateMyProfileCommand(user.getId(), request.fullName(), request.phone()));
                return ResponseEntity.ok(ApiResponseFactory.ok(response, "Profile updated successfully"));
        }

        // ----------------------------------------------------
        // PUT /users/me/password → Change own password
        // ----------------------------------------------------
        @Operation(summary = "Change my password", description = "Verifies current password and sets new one", security = @SecurityRequirement(name = "bearerAuth"))
        @PutMapping("/me/password")
        public ResponseEntity<OperationResponse<Void>> changePassword(
                        @AuthenticationPrincipal AuthenticatedUser principal,
                        @Valid @RequestBody ChangePasswordRequestDTO request) {
                var user = getUserUseCase.getDomainUserByUsernameOrEmail(null, principal.username());
                changeMyPasswordUseCase.changeMyPassword(
                                new ChangeMyPasswordCommand(user.getId(), request.currentPassword(),
                                                request.newPassword()));
                return ResponseEntity.ok(ApiResponseFactory.noContent("Password changed successfully"));
        }

        // ----------------------------------------------------
        // GET /users/{id} → Get by ID
        // ----------------------------------------------------
        @Operation(summary = "Get user by ID", description = "Requires role ADMIN, scope user:read or be the owner", security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('SCOPE_user:read') or hasRole('ADMIN') or @userSec.isOwner(authentication, #id)")
        public ResponseEntity<OperationResponse<UserResponse>> get(@PathVariable Long id) {
                var user = getUserUseCase.getUserById(id);
                return ResponseEntity.ok(ApiResponseFactory.ok(user, "User retrieved successfully"));
        }

        // ----------------------------------------------------
        // GET /users/{id}/snapshot → Internal snapshot by ID
        // ----------------------------------------------------
        @GetMapping("/{id}/snapshot")
        @PreAuthorize("hasRole('SERVICE_AUTH')")
        public ResponseEntity<OperationResponse<UserCredentialsDTO>> getSnapshot(@PathVariable Long id) {
                var user = getUserUseCase.getDomainUserById(id);
                var credentials = new UserCredentialsDTO(
                                user.getId(),
                                user.getEmail(),
                                user.getEmail(),
                                user.getPasswordHash(),
                                user.getRoles(),
                                user.getStatus().name(),
                                user.isEmailVerified(),
                                user.getStatus().name().equals("ACTIVE") && user.isEmailVerified());
                return ResponseEntity.ok(ApiResponseFactory.ok(credentials, "Snapshot retrieved successfully"));
        }

        // ----------------------------------------------------
        // GET /users/by-username → Filter by username
        // ----------------------------------------------------
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

        // ----------------------------------------------------
        // GET /users/by-email → Filter by email
        // ----------------------------------------------------
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

        // ----------------------------------------------------
        // GET /users/internal/credentials → Internal microservice access
        // ----------------------------------------------------
        @GetMapping("/internal/credentials")
        @PreAuthorize("hasRole('SERVICE_AUTH')")
        public ResponseEntity<OperationResponse<UserCredentialsDTO>> getCredentials(
                        @RequestParam(required = false) String username,
                        @RequestParam(required = false) String email) {
                var user = getUserUseCase.getDomainUserByUsernameOrEmail(username, email);
                var credentials = new UserCredentialsDTO(
                                user.getId(),
                                user.getEmail(),
                                user.getEmail(),
                                user.getPasswordHash(),
                                user.getRoles(),
                                user.getStatus().name(),
                                user.isEmailVerified(),
                                user.getStatus().name().equals("ACTIVE") && user.isEmailVerified());
                return ResponseEntity.ok(ApiResponseFactory.ok(credentials, "Credentials retrieved successfully"));
        }

        // ----------------------------------------------------
        // GET /users → List with filters
        // ----------------------------------------------------
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

        // ----------------------------------------------------
        // GET /users/exists → Check existence
        // ----------------------------------------------------
        @Operation(summary = "Check if user exists", description = "Requires role ADMIN or scope user:read", security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/exists")
        @PreAuthorize("hasAuthority('SCOPE_user:read') or hasRole('ADMIN') or hasRole('SERVICE_AUTH')")
        public ResponseEntity<OperationResponse<Boolean>> exists(
                        @RequestParam(required = false) String username,
                        @RequestParam(required = false) String email) {
                boolean exists = existsUserUseCase.existsByUsernameOrEmail(username, email);
                return ResponseEntity.ok(ApiResponseFactory.ok(exists, "User existence check completed"));
        }

        // ----------------------------------------------------
        // PUT /users/{id}/roles → Update user roles
        // ----------------------------------------------------
        @Operation(summary = "Update user roles", description = "Requires role ADMIN", security = @SecurityRequirement(name = "bearerAuth"))
        @PutMapping("/{id}/roles")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<OperationResponse<Void>> updateUserRoles(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateUserRolesRequestDTO request) {
                updateUserRolesUseCase.updateUserRoles(id, request);
                return ResponseEntity.ok(ApiResponseFactory.noContent("User roles updated successfully"));
        }

        // ----------------------------------------------------
        // PUT /users/{id}/activate → Activate user account
        // SECURITY: Only callable by auth_service with SERVICE_AUTH role
        // ----------------------------------------------------
        @PutMapping("/{id}/activate")
        @PreAuthorize("hasRole('SERVICE_AUTH')")
        public ResponseEntity<OperationResponse<Void>> activate(@PathVariable Long id) {
                activateUserUseCase.activate(id);
                return ResponseEntity.ok(ApiResponseFactory.noContent("User activated successfully"));
        }
}