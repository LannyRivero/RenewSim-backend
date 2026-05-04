package com.renewsim.backend.auth_service.infrastructure.config;

import com.renewsim.backend.auth_service.application.port.in.*;
import com.renewsim.backend.auth_service.application.port.out.*;
import com.renewsim.backend.auth_service.application.service.*;
import com.renewsim.backend.auth_service.application.validator.CredentialsValidator;
import com.renewsim.backend.auth_service.application.validator.UserAccountValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de beans para la capa de aplicación.
 * 
 * Responsable de ensamblar los casos de uso inyectando sus dependencias,
 * manteniendo la capa de aplicación libre de anotaciones de framework.
 * 
 * @since 1.2.0
 */
@Configuration
public class ApplicationServiceConfig {

    @Bean
    public LogoutUseCase logoutUseCase(
            TokenProvider tokenProvider,
            TokenBlacklistPort tokenBlacklistPort,
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            UserAccountGateway userAccountGateway,
            TransactionalPort transactionalPort,
            TimeProvider timeProvider) {
        return new LogoutService(
                tokenProvider,
                tokenBlacklistPort,
                refreshTokenRepositoryPort,
                userAccountGateway,
                transactionalPort,
                timeProvider.getClock());
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            UserAccountGateway userAccountGateway,
            TokenProvider tokenProvider,
            TransactionalPort transactionalPort,
            TimeProvider timeProvider,
            UserAccountValidator userAccountValidator) {
        return new RefreshTokenService(
                refreshTokenRepositoryPort,
                userAccountGateway,
                tokenProvider,
                transactionalPort,
                timeProvider.getClock(),
                userAccountValidator);
    }

    @Bean
    public TokenTimeService tokenTimeService(
            @Value("${jwt.access-token.expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token.expiration}") long refreshTokenExpiration) {
        return new TokenTimeService(accessTokenExpiration, refreshTokenExpiration);
    }

    @Bean
    public CredentialsValidator credentialsValidator(PasswordEncoderPort passwordEncoder) {
        return new CredentialsValidator(passwordEncoder);
    }

    @Bean
    public UserAccountValidator userAccountValidator() {
        return new UserAccountValidator();
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserAccountGateway userAccountGateway,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            EmailPort emailPort,
            TransactionalPort transactionalPort,
            @Value("${app.email.verification.expiration-hours:48}") int verificationExpirationHours) {
        return new RegisterUserService(
                userAccountGateway,
                emailVerificationTokenRepository,
                emailPort,
                transactionalPort,
                verificationExpirationHours);
    }

    @Bean
    public LoginUseCase loginUseCase(
            UserAccountGateway userAccountGateway,
            CredentialsValidator credentialsValidator,
            TokenProvider tokenProvider,
            RefreshTokenRepositoryPort refreshTokenRepository,
            TransactionalPort transactionalPort,
            TokenTimeService tokenTimeService,
            TimeProvider timeProvider) {
        return new LoginService(
                userAccountGateway,
                credentialsValidator,
                tokenProvider,
                refreshTokenRepository,
                transactionalPort,
                tokenTimeService,
                timeProvider.getClock());
    }
}