package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.mapper.AuthResponseMapper;
import com.renewsim.backend.auth_service.application.port.in.AuthUseCase;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.domain.AuthValidator;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterRequestDTO;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.shared.error.ErrorMessageFactory;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.ResourceConflictException;
import static com.renewsim.backend.auth_service.domain.error.AuthErrorCode.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {

        private final UserAccountGateway userAccountGateway;
        private final AuthValidator authValidator;
        private final AuthResponseMapper authResponseMapper;

        @Override
        public AuthResponseDTO login(AuthRequestDTO request) {

                authValidator.validateCredentials(request);

                String loginInput = request.getUsername();

                UserSnapshot user = (loginInput.contains("@")
                                ? userAccountGateway.findByEmail(loginInput)
                                : userAccountGateway.findByUsername(loginInput))
                                .orElseThrow(() -> new AuthenticationException(
                                                ErrorMessageFactory.build(AUTH_INVALID_CREDENTIALS)));

                authValidator.validateUserEnable(user.enabled());

                authValidator.validatePassword(request.getPassword(), user.passwordHash());

                return authResponseMapper.toAuthResponseDTO(user);
        }

        @Override
        @Transactional
        public AuthResponseDTO register(RegisterRequestDTO request) {

                // 1- Validar credenciales mínimas
                authValidator.validateCredentials(request);

                // 2- Verificar si existe el usuario
                if (userAccountGateway.existsByUsername(request.getUsername())) {
                        throw new ResourceConflictException(
                                        AUTH_USERNAME_CONFLICT.code(),
                                        AUTH_USERNAME_CONFLICT.defaultMessage());
                }
                String passwordHash = authValidator.encodePassword(request.getPassword());

                // 3- Crear el usuario con rol por defecto ROLE_USER
                UserSnapshot user = userAccountGateway.createUser(
                                request.getUsername(),
                                passwordHash,
                                request.getEmail(),
                                Set.of(RoleName.USER));

                return authResponseMapper.toAuthResponseDTO(user);
        }

}
