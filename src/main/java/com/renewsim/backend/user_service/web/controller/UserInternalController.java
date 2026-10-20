package com.renewsim.backend.user_service.web.controller;

import com.renewsim.backend.shared.dto.ApiResponseFactory;
import com.renewsim.backend.shared.dto.OperationResponse;
import com.renewsim.backend.user_service.application.port.in.GetUserUseCase;
import com.renewsim.backend.user_service.web.dto.UserCredentialsDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management operations")
public class UserInternalController {

    private final GetUserUseCase getUserUseCase;

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
}
