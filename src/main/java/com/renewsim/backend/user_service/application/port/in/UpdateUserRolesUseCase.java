package com.renewsim.backend.user_service.application.port.in;

import com.renewsim.backend.user_service.web.dto.UpdateUserRolesRequestDTO;

public interface UpdateUserRolesUseCase {

    /**
     * Updates the set of roles assigned to a given user.
     *
     * @param userId  the ID of the user whose roles are updated
     * @param request the request containing the new list of roles
     */
    void updateUserRoles(Long userId, UpdateUserRolesRequestDTO request);
}
