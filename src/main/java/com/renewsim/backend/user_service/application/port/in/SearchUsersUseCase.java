package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.dto.PageResponse;
import com.renewsim.backend.user_service.dto.UserResponse;

/**
 * Use case for searching users based on multiple criteria.
 *
 * <p>
 * Enables more advanced searches than the basic existence check,
 * supporting pagination and filter combinations.
 * </p>
 *
 * <p>
 * Security: Requires {@code ROLE_ADMIN} or {@code SCOPE_user:read}.
 * </p>
 */
public interface SearchUsersUseCase {

    /**
     * Searches users by the given parameters.
     *
     * @param username filter by username (nullable)
     * @param email    filter by email (nullable)
     * @param enabled  filter by enabled status (nullable)
     * @param page     the page index (0-based)
     * @param size     the page size
     * @return a {@link Page} containing domain users that match the criteria
     */
    PageResponse<UserResponse> searchUsers(int page, int size, String username, String email, Boolean enabled);
}
