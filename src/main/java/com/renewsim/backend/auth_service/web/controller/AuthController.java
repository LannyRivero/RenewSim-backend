package com.renewsim.backend.auth_service.web.controller;

import com.renewsim.backend.auth_service.application.port.in.AuthUseCase;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterRequestDTO;
import com.renewsim.backend.shared.dto.ApiResponseFactory;
import com.renewsim.backend.shared.dto.OperationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/auth", produces = "application/json")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for authentication and registration of users.")
public class AuthController {

    private final AuthUseCase authUseCase;

    // ----------------------------------------------------
    // POST /auth/login → Authenticate
    // ----------------------------------------------------
    @PostMapping(value = "/login", consumes = "application/json")
    @Operation(summary = "User login", description = "Authenticate user via email/username and password. Returns a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    })
    public ResponseEntity<OperationResponse<AuthResponseDTO>> login(@Valid @RequestBody AuthRequestDTO request) {
        AuthResponseDTO response = authUseCase.login(request);
        return ResponseEntity.ok(ApiResponseFactory.ok(response, "Login successful"));
    }

    // ----------------------------------------------------
    // POST /auth/register → Register new user
    // ----------------------------------------------------
    @PostMapping(value = "/register", consumes = "application/json")
    @Operation(summary = "User registration", description = "Register a new user account. Returns a JWT access token upon success.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or user already exists", content = @Content)
    })
    public ResponseEntity<OperationResponse<AuthResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = authUseCase.register(request);
        return ResponseEntity
                .status(201)
                .body(ApiResponseFactory.created(response, "User registered successfully"));
    }
}
