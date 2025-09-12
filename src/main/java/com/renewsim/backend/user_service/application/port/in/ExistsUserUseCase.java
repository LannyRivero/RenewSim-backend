package com.renewsim.backend.user_service.application.port.in;

/**
 * Use case for checking if a user exists in the system.
 *
 * <p>Allows verification of the existence of a user by username or email.</p>
 *
 * <p>Security: Public endpoint, no authentication required.</p>
 */
public interface ExistsUserUseCase {

     /**
     * Checks if a user exists with the given username or email.
     *
     * @param username the username to check, may be {@code null}
     * @param email the email to check, may be {@code null}
     * @return {@code true} if a user exists with the given criteria, otherwise {@code false}
     */
    boolean existsByUsernameOrEmail(String username, String email);

}
