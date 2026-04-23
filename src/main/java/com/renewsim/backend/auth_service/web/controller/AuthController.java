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

        private final AuthUseCase authUseCase;
        private final LoginStep1UseCase loginStep1UseCase;
        private final LoginStep2UseCase loginStep2UseCase;
        private final ActivateAccountUseCase activateAccountUseCase;
        private final ResendOtpUseCase resendOtpUseCase;
        private final LogoutUseCase logoutUseCase;
        private final RefreshTokenUseCase refreshTokenUseCase;

        // ----------------------------------------------------
        // POST /auth/login → legacy single-factor
        // ----------------------------------------------------
        @PostMapping(value = "/login", consumes = "application/json")
        @Operation(summary = "User login (single-factor)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
        })
        public ResponseEntity<OperationResponse<AuthResponseDTO>> login(
                        @Valid @RequestBody AuthRequestDTO request) {
                AuthCommand command = new AuthCommand(request.getUsername(), request.getPassword());
                AuthResult result = authUseCase.login(command);
                AuthResponseDTO response = new AuthResponseDTO(
                        result.getToken(),
                        result.getTokenType(),
                        result.getExpiresAt(),
                        result.getUsername(),
                        result.getRoles(),
                        result.getScopes()
                );
                return ResponseEntity.ok(ApiResponseFactory.ok(response, "Login successful"));
        }

        // ----------------------------------------------------
        // POST /auth/register → Register new user
        // ----------------------------------------------------
        @PostMapping(value = "/register", consumes = "application/json")
        @Operation(summary = "User registration")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponseDTO.class))),
                        @ApiResponse(responseCode = "409", description = "User already exists", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
        })
        public ResponseEntity<OperationResponse<RegisterResponseDTO>> register(
                        @Valid @RequestBody RegisterRequestDTO request) {
                RegisterCommand command = new RegisterCommand(
                        request.getFullName(),
                        request.getPassword(),
                        request.getEmail()
                );
                RegisterResult result = authUseCase.register(command);
                RegisterResponseDTO response = new RegisterResponseDTO(
                        result.getId(),
                        result.getEmail(),
                        result.getFullName(),
                        result.getStatus(),
                        result.getMessage()
                );
                return ResponseEntity.status(201)
                                .body(ApiResponseFactory.created(response, "User registered successfully"));
        }

        // ----------------------------------------------------
        // POST /auth/login/step1 → Validate credentials, send OTP
        // ----------------------------------------------------
        @PostMapping(value = "/login/step1", consumes = "application/json")
        @Operation(summary = "2FA login step 1", description = "Validates credentials and sends OTP. Response is always generic.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "OTP sent (or silently ignored)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginStep1ResultDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                        @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
        })
        public ResponseEntity<OperationResponse<LoginStep1ResultDTO>> loginStep1(
                        @Valid @RequestBody LoginStep1RequestDTO request) {
                LoginStep1ResultDTO result = loginStep1UseCase.execute(
                                new LoginStep1Command(request.email(), request.password()));
                return ResponseEntity.ok(ApiResponseFactory.ok(result, result.message()));
        }

        // ----------------------------------------------------
        // POST /auth/login/step2 → Validate OTP, issue JWT + refresh cookie
        // ----------------------------------------------------
        @PostMapping(value = "/login/step2", consumes = "application/json")
        @Operation(summary = "2FA login step 2", description = "Validates OTP and issues JWT. Refresh token set as HttpOnly cookie.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginStep2ResultDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid or expired OTP", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                        @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
        })
        public ResponseEntity<OperationResponse<LoginStep2ResultDTO>> loginStep2(
                        @Valid @RequestBody LoginStep2RequestDTO request,
                        HttpServletResponse response) {
                LoginStep2ResultDTO result = loginStep2UseCase.execute(
                                new LoginStep2Command(request.email(), request.otpCode()));

                addRefreshTokenCookie(response, result.rawRefreshToken());

                LoginStep2ResultDTO safeResult = new LoginStep2ResultDTO(
                                result.accessToken(), result.tokenType(), result.expiresIn(),
                                result.username(), result.roles(), null);

                return ResponseEntity.ok(ApiResponseFactory.ok(safeResult, "Authentication successful"));
        }

        // ----------------------------------------------------
        // POST /auth/activate → Activate account via token
        // ----------------------------------------------------
        @PostMapping(value = "/activate", consumes = "application/json")
        @Operation(summary = "Activate user account")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Account activated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ActivateAccountResultDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid or expired token", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
        })
        public ResponseEntity<OperationResponse<ActivateAccountResultDTO>> activate(
                        @Valid @RequestBody ActivateAccountRequestDTO request) {
                ActivateAccountResultDTO result = activateAccountUseCase.execute(
                                new ActivateAccountCommand(request.token()));
                return ResponseEntity.ok(ApiResponseFactory.ok(result, result.message()));
        }

        // ----------------------------------------------------
        // POST /auth/resend-otp → Resend OTP
        // ----------------------------------------------------
        @PostMapping(value = "/resend-otp", consumes = "application/json")
        @Operation(summary = "Resend OTP", description = "Response is always generic.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "OTP resent (or silently ignored)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResendOtpResultDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                        @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
        })
        public ResponseEntity<OperationResponse<ResendOtpResultDTO>> resendOtp(
                        @Valid @RequestBody ResendOtpRequestDTO request) {
                ResendOtpResultDTO result = resendOtpUseCase.execute(
                                new ResendOtpCommand(request.email()));
                return ResponseEntity.ok(ApiResponseFactory.ok(result, result.message()));
        }

        // ----------------------------------------------------
        // POST /auth/logout → Blacklist JTI + revoke refresh tokens
        // ----------------------------------------------------
        @PostMapping(value = "/logout")
        @Operation(summary = "Logout", description = "Invalidates access token and revokes all refresh tokens.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Logged out successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LogoutResultDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content)
        })
        public ResponseEntity<OperationResponse<LogoutResultDTO>> logout(
                        @RequestHeader("Authorization") String authHeader,
                        @AuthenticationPrincipal AuthenticatedUser principal) {

                String token = authHeader.startsWith("Bearer ")
                                ? authHeader.substring(7)
                                : authHeader;

                String username = principal != null ? principal.username() : "";

                LogoutResultDTO result = logoutUseCase.execute(new LogoutCommand(token, username));
                return ResponseEntity.ok(ApiResponseFactory.ok(result, result.message()));
        }

        // ----------------------------------------------------
        // POST /auth/refresh → Rotate refresh token via HttpOnly cookie
        // ----------------------------------------------------
        @PostMapping(value = "/refresh")
        @Operation(summary = "Refresh access token", description = "Reads refresh token from HttpOnly cookie, rotates it, issues new access token.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Token refreshed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RefreshTokenResultDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", content = @Content)
        })
        public ResponseEntity<OperationResponse<RefreshTokenResultDTO>> refresh(
                        @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie,
                        HttpServletResponse response) {

                if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
                        return ResponseEntity.status(401)
                                        .body(ApiResponseFactory.ok(null, "Refresh token missing"));
                }

                RefreshTokenResultDTO result = refreshTokenUseCase.execute(
                                new RefreshTokenCommand(refreshTokenCookie));

                addRefreshTokenCookie(response, result.rawRefreshToken());

                RefreshTokenResultDTO safeResult = new RefreshTokenResultDTO(
                                result.accessToken(), result.tokenType(), result.expiresIn(),
                                result.username(), result.roles(), null);

                return ResponseEntity.ok(ApiResponseFactory.ok(safeResult, "Token refreshed successfully"));
        }

        // ----------------------------------------------------
        // Helper — cookie con SameSite=Strict (fix D2-02)
        // ----------------------------------------------------
        private void addRefreshTokenCookie(HttpServletResponse response, String rawRefreshToken) {
                ResponseCookie cookie = ResponseCookie.from("refresh_token", rawRefreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path("/api/v1/auth/refresh")
                                .maxAge(Duration.ofDays(7))
                                .sameSite("Strict")
                                .build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
}