package com.renewsim.backend.auth_service.infrastructure.config;

import com.renewsim.backend.auth_service.application.port.in.*;
import com.renewsim.backend.auth_service.application.port.out.*;
import com.renewsim.backend.auth_service.application.service.*;
import com.renewsim.backend.auth_service.application.validator.CredentialsValidator;
import com.renewsim.backend.auth_service.domain.service.OtpGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

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
    public LoginStep1UseCase loginStep1UseCase(
            UserAccountGateway userAccountGateway,
            OtpCodeRepositoryPort otpCodeRepositoryPort,
            OtpGenerator otpGenerator,
            CredentialsValidator credentialsValidator,
            PasswordEncoderPort passwordEncoder,
            EmailPort emailPort,
            TransactionalPort transactionalPort,
            Clock clock) {
        return new LoginStep1Service(
                userAccountGateway,
                otpCodeRepositoryPort,
                otpGenerator,
                credentialsValidator,
                passwordEncoder,
                emailPort,
                transactionalPort,
                clock

        );
    }

    @Bean
    public LoginStep2UseCase loginStep2UseCase(
            UserAccountGateway userAccountGateway,
            OtpCodeRepositoryPort otpCodeRepositoryPort,
            CredentialsValidator credentialsValidator,
            PasswordEncoderPort passwordEncoder,
            TokenProvider tokenProvider,
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            TransactionalPort transactionalPort,
            Clock clock) {
        return new LoginStep2Service(
                userAccountGateway,
                otpCodeRepositoryPort,
                refreshTokenRepositoryPort,
                tokenProvider,
                passwordEncoder,
                transactionalPort,
                clock);

    }

    @Bean
    public LogoutUseCase logoutUseCase(
            TokenProvider tokenProvider,
            TokenBlacklistPort tokenBlacklistPort,
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            UserAccountGateway userAccountGateway,
            TransactionalPort transactionalPort,
            Clock clock) {
        return new LogoutService(
                tokenProvider,
                tokenBlacklistPort,
                refreshTokenRepositoryPort,
                userAccountGateway,
                transactionalPort,
                clock);
    }

    @Bean
    public ActivateAccountUseCase activateAccountUseCase(
            ActivationTokenRepositoryPort activationTokenRepositoryPort,
            UserAccountGateway userAccountGateway,
            TransactionalPort transactionalPort,
            Clock clock) {
        return new ActivateAccountService(
                activationTokenRepositoryPort,
                userAccountGateway,
                transactionalPort,
                clock

        );
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            UserAccountGateway userAccountGateway,
            TokenProvider tokenProvider,
            TransactionalPort transactionalPort,
            Clock clock) {
        return new RefreshTokenService(
                refreshTokenRepositoryPort,
                userAccountGateway,
                tokenProvider,
                transactionalPort,
                clock

        );
    }

    @Bean
    public ResendOtpUseCase resendOtpUseCase(
            UserAccountGateway userAccountGateway,
            OtpCodeRepositoryPort otpCodeRepositoryPort,
            OtpGenerator otpGenerator,
            PasswordEncoderPort passwordEncoder,
            EmailPort emailPort,
            TransactionalPort transactionalPort,
            Clock clock) {
        return new ResendOtpService(
                userAccountGateway,
                otpCodeRepositoryPort,
                otpGenerator,
                passwordEncoder,
                emailPort,
                transactionalPort,
                clock);
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
}