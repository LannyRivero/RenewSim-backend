package com.renewsim.backend.auth_service.web.controller;

import com.renewsim.backend.auth_service.application.command.*;
import com.renewsim.backend.auth_service.application.port.in.*;
import com.renewsim.backend.auth_service.application.result.*;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.web.dto.*;
import com.renewsim.backend.shared.dto.ApiResponseFactory;
import com.renewsim.backend.shared.dto.OperationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping(value = "/api/v1/auth", produces = "application/json")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for authentication and registration of users.")
public class AuthController {

    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final RegisterUserUseCase registerUserUseCase;

    // ----------------------------------------------------
    // POST /auth/register → Create new user account
    // ----------------------------------------------------
    @PostMapping(value = "/register", consumes = "application/json")
    @Operation(summary = "Register new user", description = "Creates a new user account and sends verification email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already registered", content = @Content)
    })
    public ResponseEntity<OperationResponse<RegisterResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO request) {
        var result = registerUserUseCase.execute(
                new RegisterCommand(request.fullName(), request.password(), request.email()));
        var response = new RegisterResponseDTO(
                result.id(), result.email(), result.fullName(), result.status(), result.message());
        return ResponseEntity.status(201)
                .body(ApiResponseFactory.created(response, "User registered successfully. Check your email to verify your account."));
    }

    // ----------------------------------------------------
    // POST /auth/logout → Blacklist JTI + revoke refresh tokens
    // ----------------------------------------------------
    @PostMapping(value = "/logout")
    @Operation(summary = "Logout", description = "Invalidates access token and revokes all refresh tokens.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LogoutResult.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content)
    })
    public ResponseEntity<OperationResponse<LogoutResult>> logout(
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;

        String username = principal != null ? principal.username() : "";

        LogoutResult result = logoutUseCase.execute(new LogoutCommand(token, username));
        return ResponseEntity.ok(ApiResponseFactory.ok(result, result.message()));
    }

    // ----------------------------------------------------
    // POST /auth/refresh → Rotate refresh token via HttpOnly cookie
    // ----------------------------------------------------
    @PostMapping(value = "/refresh")
    @Operation(summary = "Refresh access token", description = "Reads refresh token from HttpOnly cookie, rotates it, issues new access token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RefreshTokenResult.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", content = @Content)
    })
    public ResponseEntity<OperationResponse<RefreshTokenResult>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
            HttpServletResponse response) {

        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            return ResponseEntity.status(401)
                    .body(ApiResponseFactory.ok(null, "Refresh token missing"));
        }

        RefreshTokenResult result = refreshTokenUseCase.execute(
                new RefreshTokenCommand(refreshTokenCookie));

        addRefreshTokenCookie(response, result.rawRefreshToken());

        RefreshTokenResult safeResult = new RefreshTokenResult(
                result.accessToken(), result.tokenType(), result.expiresIn(),
                result.username(), result.roles(), null);

        return ResponseEntity.ok(ApiResponseFactory.ok(safeResult, "Token refreshed successfully"));
    }

    // ----------------------------------------------------
    // Helper — cookie con SameSite=Strict
    // ----------------------------------------------------
    private void addRefreshTokenCookie(HttpServletResponse response, String rawRefreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", rawRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}