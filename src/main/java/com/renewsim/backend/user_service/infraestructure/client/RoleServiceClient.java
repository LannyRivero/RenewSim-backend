package com.renewsim.backend.user_service.infraestructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.renewsim.backend.role_service.dto.RoleDTO;
import com.renewsim.backend.shared.dto.OperationResponse;

@FeignClient(name = "role-service", contextId = "userRoleClient", url = "${role-service.url}", configuration = FeignRoleConfig.class)
public interface RoleServiceClient {

    @GetMapping("/api/v1/roles/exists/{name}")
    OperationResponse<Boolean> existsRole(@PathVariable("name") String name);

    @GetMapping("/api/v1/roles/by-name/{name}")
    OperationResponse<RoleDTO> findByName(@PathVariable("name") String name);

}
