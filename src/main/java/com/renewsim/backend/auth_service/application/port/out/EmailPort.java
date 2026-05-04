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
     * Sends an email verification link to the given recipient.
     *
     * <p>
     * The {@code verificationToken} is the raw token that must be embedded
     * in the verification URL. The adapter must never log this token.
     *
     * <p>
     * This email is sent during user registration to verify email ownership
     * before allowing login.
     *
     * @param toEmail           recipient email address — never null or blank
     * @param username          user's display name for personalization — never null or blank
     * @param verificationToken the raw verification token — never null or blank
     */
    void sendVerificationEmail(String toEmail, String username, String verificationToken);

    /**
     * Sends a password reset link to the given recipient.
     *
     * <p>
     * The {@code resetToken} is the raw token that must be embedded in the
     * password reset URL. The adapter must never log this token.
     *
     * @param toEmail    recipient email address — never null or blank
     * @param username   user's display name for personalization — never null or blank
     * @param resetToken the raw password reset token — never null or blank
     */
    void sendPasswordResetEmail(String toEmail, String username, String resetToken);
}