package com.renewsim.backend.role_service.application.port.in;

import com.renewsim.backend.role_service.web.dto.RoleDTO;

public interface GetRoleByIdUseCase {

    RoleDTO getById(Long roleId);
}
