package com.renewsim.backend.role_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.renewsim.backend.shared.dto.OperationResponse;
import com.renewsim.backend.user_service.dto.UpdateUserRolesRequestDTO;
import com.renewsim.backend.user_service.infrastructure.client.FeignRoleConfig;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@FeignClient(name = "user-service", contextId = "roleUserClient", url = "${user-service.url}",configuration = FeignRoleConfig.class)
public interface UserServiceClient {
    @PutMapping("/api/v1/users/{id}/roles")
    OperationResponse<Void> updateUserRoles(@PathVariable("id") Long userId,
                         @RequestBody UpdateUserRolesRequestDTO request);
}

