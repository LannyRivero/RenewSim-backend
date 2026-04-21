package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LoginStep1Command;
import com.renewsim.backend.auth_service.application.port.in.LoginStep1UseCase;
import com.renewsim.backend.auth_service.application.port.out.OtpCodeRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.LoginStep1ResultDTO;
import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.domain.service.OtpGenerator;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginStep1Service implements LoginStep1UseCase {

    private static final int OTP_EXPIRES_IN_SECONDS = 300;

    private final UserAccountGateway userAccountGateway;
    private final OtpCodeRepositoryPort otpCodeRepositoryPort;
    private final OtpGenerator otpGenerator;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginStep1ResultDTO execute(LoginStep1Command command) {
        // Intentionally generic response — never reveal whether email exists
        UserSnapshot user = userAccountGateway.findByEmail(command.email())
                .orElse(null);

        if (user == null || !user.enabled()) {
            log.warn("Login step1 attempted for unknown or disabled email");
            return new LoginStep1ResultDTO(
                    "If your account exists and is active, you will receive an OTP.",
                    OTP_EXPIRES_IN_SECONDS);
        }

        if (!passwordEncoder.matches(command.password(), user.passwordHash())) {
            log.warn("Login step1 failed: invalid password for email={}", command.email());
            return new LoginStep1ResultDTO(
                    "If your account exists and is active, you will receive an OTP.",
                    OTP_EXPIRES_IN_SECONDS);
        }

        // Invalidate any previous OTP for this user
        otpCodeRepositoryPort.invalidateAllByUserId(user.id(), OtpCode.Purpose.LOGIN);

        // Generate and persist new OTP
        String rawOtp = otpGenerator.generate();
        String hashedOtp = passwordEncoder.encode(rawOtp);
        OtpCode otpCode = OtpCode.issue(user.id(), hashedOtp, OtpCode.Purpose.LOGIN);
        otpCodeRepositoryPort.save(otpCode);

        // TODO: send OTP via email (EmailPort — Task 2.9)
        log.info("OTP generated for userId={} [email delivery pending]", user.id());

        return new LoginStep1ResultDTO(
                "If your account exists and is active, you will receive an OTP.",
                OTP_EXPIRES_IN_SECONDS);
    }
}