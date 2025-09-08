package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.mapper.AuthResponseMapper;
import com.renewsim.backend.auth_service.application.port.in.AuthUseCase;
import com.renewsim.backend.auth_service.application.port.out.ScopePolicy;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.domain.AuthValidator;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.domain.TokenTimeService;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role.RoleName;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.ResourceConflictException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {

        private final UserAccountGateway userAccountGateway;
        private final AuthValidator authValidator;
        private final AuthResponseMapper authResponseMapper;

        @Override
        public AuthResponseDTO login(AuthRequestDTO request) {

                authValidator.validateCredentials(request);

                UserSnapshot user = userAccountGateway.findByUsername(request.getUsername())
                                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

                authValidator.validateUserEnable(user.enabled());

                return authResponseMapper.toAuthResponseDTO(user);
        }

        @Override
        @Transactional
        public AuthResponseDTO register(AuthRequestDTO request) {

                // 1- validamos datos de entrada
                authValidator.validateCredentials(request);

                // 2-Verificar si existe el usuario
                if (userAccountGateway.existsByUsername(request.getUsername())) {
                        throw new ResourceConflictException("Username already exists" + request.getUsername());
                }
                // 3- Crear el usuario con rol por defecto ROLE_USER
                UserSnapshot user = userAccountGateway.createUser(
                                request.getUsername(),
                                request.getPassword(),
                                Set.of(RoleName.USER));

                return authResponseMapper.toAuthResponseDTO(user);

        }
}
