package com.renewsim.backend.user_service.infraestructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.renewsim.backend.user_service.application.port.in.CreateUserUseCase;
import com.renewsim.backend.user_service.application.port.in.ExistsUserUseCase;
import com.renewsim.backend.user_service.application.port.in.GetUserUseCase;
import com.renewsim.backend.user_service.application.port.in.ListUsersUseCase;
import com.renewsim.backend.user_service.dto.PageResponse;
import com.renewsim.backend.user_service.dto.UserCreateRequest;
import com.renewsim.backend.user_service.dto.UserCredentialsDTO;
import com.renewsim.backend.user_service.dto.UserFilterRequest;
import com.renewsim.backend.user_service.dto.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final CreateUserUseCase createUserUseCase;
    private final ExistsUserUseCase existsUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_user:write') or hasRole('ADMIN') or hasRole('SERVICE_AUTH')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest req) {
        UserResponse created = createUserUseCase.createUser(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasRole('ADMIN') or @userSec.isOwner(authentication, #id)")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(getUserUseCase.getUserById(id));
    }

    @GetMapping("/by-username")
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasRole('ADMIN') )")
    public ResponseEntity<UserResponse> getByUsername(@RequestParam String username) {
        var filters = new UserFilterRequest(username, null, null);
        var results = listUsersUseCase.listUsers(0, 1, filters);
        if (results.content().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(results.content().get(0));
    }

    @GetMapping("/by-email")
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getByEmail(@RequestParam String email) {
        var filters = new UserFilterRequest(null, email, null);
        var results = listUsersUseCase.listUsers(0, 1, filters);
        if (results.content().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(results.content().get(0));
    }

    @GetMapping("/internal/credentials")
    @PreAuthorize("hasRole('SERVICE_AUTH')")
    public ResponseEntity<UserCredentialsDTO> getCredentials(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {

        var user = getUserUseCase.getDomainUserByUsernameOrEmail(username, email);

        return ResponseEntity.ok(new UserCredentialsDTO(
                user.username(),
                user.email(),
                user.passwordHash(),
                user.roles(),
                user.enabled()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean enabled) {
        return ResponseEntity
                .ok(listUsersUseCase.listUsers(page, size, new UserFilterRequest(username, email, enabled)));
    }

    @GetMapping("/exists")
    @PreAuthorize("hasAuthority('SCOPE_user:read') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> exists(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(existsUserUseCase.existsByUsernameOrEmail(username, email));
    }

}
