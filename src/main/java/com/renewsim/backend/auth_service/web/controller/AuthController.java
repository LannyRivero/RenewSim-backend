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

        private final LoginStep1UseCase loginStep1UseCase;
        private final LoginStep2UseCase loginStep2UseCase;
        private final ActivateAccountUseCase activateAccountUseCase;
        private final ResendOtpUseCase resendOtpUseCase;
        private final LogoutUseCase logoutUseCase;
        private final RefreshTokenUseCase refreshTokenUseCase;

        // ----------------------------------------------------
        // POST /auth/login/step1 → Validate credentials, send OTP
        // ----------------------------------------------------
        @PostMapping(value = "/login/step1", consumes = "application/json")
        @Operation(summary = "2FA login step 1", description = "Validates credentials and sends OTP. Response is always generic.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "OTP sent (or silently ignored)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginStep1Result.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                        @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
        })
        public ResponseEntity<OperationResponse<LoginStep1Result>> loginStep1(
                        @Valid @RequestBody LoginStep1RequestDTO request) {
                LoginStep1Result result = loginStep1UseCase.execute(
                                new LoginStep1Command(request.email(), request.password()));
                return ResponseEntity.ok(ApiResponseFactory.ok(result, result.message()));
        }

        // ----------------------------------------------------
        // POST /auth/login/step2 → Validate OTP, issue JWT + refresh cookie
        // ----------------------------------------------------
        @PostMapping(value = "/login/step2", consumes = "application/json")
        @Operation(summary = "2FA login step 2", description = "Validates OTP and issues JWT. Refresh token set as HttpOnly cookie.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginStep2Result.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid or expired OTP", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                        @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
        })
        public ResponseEntity<OperationResponse<LoginStep2Result>> loginStep2(
                        @Valid @RequestBody LoginStep2RequestDTO request,
                        HttpServletResponse response) {
                LoginStep2Result result = loginStep2UseCase.execute(
                                new LoginStep2Command(request.email(), request.otpCode()));

                addRefreshTokenCookie(response, result.rawRefreshToken());

                LoginStep2Result safeResult = new LoginStep2Result(
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
                        @ApiResponse(responseCode = "200", description = "Account activated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ActivateAccountResult.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid or expired token", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
        })
        public ResponseEntity<OperationResponse<ActivateAccountResult>> activate(
                        @Valid @RequestBody ActivateAccountRequestDTO request) {
                ActivateAccountResult result = activateAccountUseCase.execute(
                                new ActivateAccountCommand(request.token()));
                return ResponseEntity.ok(ApiResponseFactory.ok(result, result.message()));
        }

        // ----------------------------------------------------
        // POST /auth/resend-otp → Resend OTP
        // ----------------------------------------------------
        @PostMapping(value = "/resend-otp", consumes = "application/json")
        @Operation(summary = "Resend OTP", description = "Response is always generic.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "OTP resent (or silently ignored)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResendOtpResult.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
                        @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
        })
        public ResponseEntity<OperationResponse<ResendOtpResult>> resendOtp(
                        @Valid @RequestBody ResendOtpRequestDTO request) {
                ResendOtpResult result = resendOtpUseCase.execute(
                                new ResendOtpCommand(request.email()));
                return ResponseEntity.ok(ApiResponseFactory.ok(result, result.message()));
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