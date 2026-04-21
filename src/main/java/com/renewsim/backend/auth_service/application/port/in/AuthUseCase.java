package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterRequestDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterResponseDTO;

public interface AuthUseCase {
    AuthResponseDTO login(AuthRequestDTO request);

    RegisterResponseDTO register(RegisterRequestDTO request);
}