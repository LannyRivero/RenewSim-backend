package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.port.in.AuthUseCase;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.mapper.AuthResponseMapper;
import com.renewsim.backend.auth_service.domain.AuthValidator;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterRequestDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterResponseDTO;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.error.ErrorMessageFactory;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.ResourceConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.renewsim.backend.auth_service.domain.error.AuthErrorCode.*;

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
        public RegisterResponseDTO register(RegisterRequestDTO request) {
                // Validación estructural cubierta por @Valid en el controller.
                // Aquí solo validamos invariantes de negocio.
                if (userAccountGateway.existsByEmail(request.email())) {
                        throw new ResourceConflictException(
                                        AUTH_EMAIL_CONFLICT.code(),
                                        AUTH_EMAIL_CONFLICT.defaultMessage());
                }

                UserSnapshot user = userAccountGateway.createUser(
                                request.fullName(),
                                request.password(),
                                request.email(),
                                Set.of(RoleName.USER));

                return new RegisterResponseDTO(
                                user.id(),
                                user.email(),
                                user.fullName(),
                                user.status(),
                                "User registered successfully. Please check your email to activate your account.");
        }
}