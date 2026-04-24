package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.ResendOtpCommand;
import com.renewsim.backend.auth_service.application.port.in.ResendOtpUseCase;
import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import com.renewsim.backend.auth_service.application.port.out.OtpCodeRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.PasswordEncoderPort;
import com.renewsim.backend.auth_service.application.port.out.TransactionalPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.ResendOtpResult;
import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.domain.service.OtpGenerator;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResendOtpService implements ResendOtpUseCase {

    private static final Logger log = LoggerFactory.getLogger(ResendOtpService.class);

    private static final int OTP_EXPIRES_IN_SECONDS = 300;

    private final UserAccountGateway userAccountGateway;
    private final OtpCodeRepositoryPort otpCodeRepositoryPort;
    private final OtpGenerator otpGenerator;
    private final PasswordEncoderPort passwordEncoder;
    private final EmailPort emailPort;
    private final TransactionalPort transactionalPort;
    private final Clock clock;

    public ResendOtpService(
            UserAccountGateway userAccountGateway,
            OtpCodeRepositoryPort otpCodeRepositoryPort,
            OtpGenerator otpGenerator,
            PasswordEncoderPort passwordEncoder,
            EmailPort emailPort,
            TransactionalPort transactionalPort,
            Clock clock) {
        this.userAccountGateway = userAccountGateway;
        this.otpCodeRepositoryPort = otpCodeRepositoryPort;
        this.otpGenerator = otpGenerator;
        this.passwordEncoder = passwordEncoder;
        this.emailPort = emailPort;
        this.transactionalPort = transactionalPort;
        this.clock = clock;
    }

    @Override
        public ResendOtpResult execute(ResendOtpCommand command) {
                return transactionalPort.execute(() -> executeInternal(command));
        }

    private ResendOtpResult executeInternal(ResendOtpCommand command) {

        // Generic response — never reveal whether email exists
        UserSnapshot user = userAccountGateway.findByEmail(command.email())
                .orElse(null);

        if (user == null || !user.enabled()) {
            log.warn("ResendOtp attempted for unknown or disabled email");
            return genericResponse();
        }

        // Invalidate all previous OTPs for this user
        otpCodeRepositoryPort.invalidateAllByUserId(user.id(), OtpCode.Purpose.LOGIN);

        // Generate and persist new OTP
        String rawOtp = otpGenerator.generate();
        String hashedOtp = passwordEncoder.encode(rawOtp);
        OtpCode otpCode = OtpCode.issue(user.id(), hashedOtp, OtpCode.Purpose.LOGIN);
        otpCodeRepositoryPort.save(otpCode);

        // Deliver OTP via email — adapter is profile-specific
        emailPort.sendOtp(user.email(), rawOtp, OTP_EXPIRES_IN_SECONDS);
        log.info("OTP resent for userId={}", user.id());

        return genericResponse();
    }

    private static ResendOtpResult genericResponse() {
        return new ResendOtpResult(
                "If your account exists and is active, a new OTP has been sent.",
                OTP_EXPIRES_IN_SECONDS);
    }
}