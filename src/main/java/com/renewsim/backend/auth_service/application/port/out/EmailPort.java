package com.renewsim.backend.auth_service.application.port.out;

/**
 * Output port for sending transactional emails.
 *
 * <p>
 * Defines the contract that the application layer uses to deliver email
 * notifications. The application layer is completely decoupled from the
 * transport mechanism (SMTP, SES, logging stub, etc.) — concrete
 * implementations live in the infrastructure layer.
 *
 * <p>
 * All methods are fire-and-forget from the caller's perspective. Any
 * delivery failure must be handled by the adapter (e.g., retried, logged or
 * re-thrown as a domain-neutral exception).
 */
public interface EmailPort {

    /**
     * Sends a one-time password to the given recipient.
     *
     * <p>
     * The {@code rawOtp} is the plain-text code the user must enter.
     * It is the caller's responsibility to <em>not</em> persist the raw value
     * after this call.
     *
     * @param toEmail          recipient email address — never null or blank
     * @param rawOtp           the plain-text OTP code — never null or blank
     * @param expiresInSeconds TTL of the OTP in seconds, used for UX copy
     */
    void sendOtp(String toEmail, String rawOtp, int expiresInSeconds);

    /**
     * Sends an account-activation link to the given recipient.
     *
     * <p>
     * The {@code activationToken} is the raw (un-hashed) token. The
     * adapter must embed it into the activation URL and must <em>never</em>
     * log it.
     *
     * @param toEmail         recipient email address — never null or blank
     * @param activationToken the raw activation token — never null or blank
     */
    void sendActivationEmail(String toEmail, String activationToken);
}