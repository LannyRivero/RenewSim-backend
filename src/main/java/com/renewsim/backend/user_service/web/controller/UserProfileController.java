package com.renewsim.backend.user_service.web.controller;

import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.shared.dto.ApiResponseFactory;
import com.renewsim.backend.shared.dto.OperationResponse;
import com.renewsim.backend.user_service.application.command.ChangeMyPasswordCommand;
import com.renewsim.backend.user_service.application.command.UpdateMyProfileCommand;
import com.renewsim.backend.user_service.application.port.in.GetMyProfileUseCase;
import com.renewsim.backend.user_service.application.port.in.GetUserUseCase;
import com.renewsim.backend.user_service.application.port.in.UpdateMyProfileUseCase;
import com.renewsim.backend.user_service.application.port.in.ChangeMyPasswordUseCase;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import com.renewsim.backend.user_service.web.dto.UpdateMyProfileRequestDTO;
import com.renewsim.backend.user_service.web.dto.ChangePasswordRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management operations")
public class UserProfileController {

    private final GetUserUseCase getUserUseCase;
    private final GetMyProfileUseCase getMyProfileUseCase;
    private final UpdateMyProfileUseCase updateMyProfileUseCase;
    private final ChangeMyPasswordUseCase changeMyPasswordUseCase;

    @Operation(summary = "Get my profile", description = "Returns the authenticated user's profile", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<OperationResponse<UserResponse>> getMe(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        var user = getUserUseCase.getDomainUserByUsernameOrEmail(principal.username(), null);
        var response = getMyProfileUseCase.getMyProfile(user.getId());
        return ResponseEntity.ok(ApiResponseFactory.ok(response, "Profile retrieved successfully"));
    }

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
}
