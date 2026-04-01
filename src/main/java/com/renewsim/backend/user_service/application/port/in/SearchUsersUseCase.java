package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.application.port.in.common.SearchUseCase;
import com.renewsim.backend.user_service.web.dto.PageResponse;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import com.renewsim.backend.user_service.web.dto.UserSearchCriteria;

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
public interface SearchUsersUseCase extends SearchUseCase<UserResponse, UserSearchCriteria> {

    /**
     * Searches users by the given parameters.
     *
     * @param username filter by username (nullable)
     * @param email    filter by email (nullable)
     * @param enabled  filter by enabled status (nullable)
     * @param page     the page index (0-based)
     * @param size     the page size
     * @return a {@link PageResponse} containing domain users that match the criteria
     */
    PageResponse<UserResponse> searchUsers(int page, int size, String username, String email, Boolean enabled);
}
