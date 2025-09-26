package com.renewsim.backend.shared.exception;
/**
 * Exception thrown when attempting to remove the last ADMIN role from the system.
 */
public class LastAdminRemovalException extends RuntimeException {

    public LastAdminRemovalException(String message) {
        super(message);
    }
}

