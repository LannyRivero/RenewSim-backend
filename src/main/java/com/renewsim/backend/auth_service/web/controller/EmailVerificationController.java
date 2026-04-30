package com.renewsim.backend.auth_service.web.controller;

import com.renewsim.backend.auth_service.application.service.ResendVerificationEmailUseCase;
import com.renewsim.backend.auth_service.application.service.VerifyEmailUseCase;
import com.renewsim.backend.auth_service.web.dto.MessageResponse;
import com.renewsim.backend.auth_service.web.dto.ResendVerificationRequest;
import com.renewsim.backend.auth_service.web.dto.VerifyEmailRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for email verification operations.
 * Provides endpoints for verifying email addresses and resending verification emails.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/email-verification")
@RequiredArgsConstructor
@Tag(name = "Email Verification", description = "Email verification management endpoints")
public class EmailVerificationController {

    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationEmailUseCase resendVerificationEmailUseCase;

    /**
     * Verify user's email address using verification token.
     * 
     * @param request contains the verification token
     * @return success message
     */
    @PostMapping("/verify")
    @Operation(
        summary = "Verify email address",
        description = "Verifies a user's email address using the token sent via email"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Email verified successfully",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid, expired, or already used token",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
        )
    })
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        log.info("Email verification requested");
        
        verifyEmailUseCase.execute(request.token());
        
        return ResponseEntity.ok(new MessageResponse("Email verified successfully. You can now log in."));
    }

    /**
     * Resend verification email to user.
     * 
     * @param request contains the user's email address
     * @return success message
     */
    @PostMapping("/resend")
    @Operation(
        summary = "Resend verification email",
        description = "Sends a new verification email to the specified address. Rate limited to prevent abuse."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Verification email sent successfully",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Email already verified or invalid request",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Too many requests - rate limit exceeded",
            content = @Content
        )
    })
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        log.info("Resend verification requested for email={}", maskEmail(request.email()));
        
        resendVerificationEmailUseCase.execute(request.email());
        
        return ResponseEntity.ok(new MessageResponse("Verification email sent. Please check your inbox."));
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf("@");
        return at <= 2 ? "***" + email.substring(at) : email.substring(0, 2) + "***" + email.substring(at);
    }
}