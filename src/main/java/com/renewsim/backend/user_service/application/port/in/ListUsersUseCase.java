package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.application.port.in.common.ListUseCase;
import com.renewsim.backend.user_service.dto.PageResponse;
import com.renewsim.backend.user_service.dto.UserFilterRequest;
import com.renewsim.backend.user_service.dto.UserResponse;

/**
 * Use case for listing users with filtering and pagination.
 *
 * <p>
 * Provides capabilities to filter by username, email, or enabled status,
 * returning results in a paginated format.
 * </p>
 *
 * <p>
 * Security: Requires {@code ROLE_ADMIN} or {@code SCOPE_user:read}.
 * </p>
 */
public interface ListUsersUseCase extends ListUseCase<UserResponse> {

    /**
     * Lists users based on filter criteria.
     *
     * @param page   the page index (0-based)
     * @param size   the page size
     * @param filter the {@link UserFilterRequest} containing filter conditions
     * @return a {@link PageResponse} with the list of matching users
     */
    PageResponse<UserResponse> listUsers(int page, int size, UserFilterRequest filter);

}
