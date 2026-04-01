package com.renewsim.backend.user_service.web.dto;

/**
 * Criteria object for searching users.
 * 
 * <p>
 * Add more fields as needed to support flexible queries.
 * </p>
 */
public record UserSearchCriteria(
        String username,
        String email,
        Boolean enabled) {
}
