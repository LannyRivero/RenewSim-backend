package com.renewsim.backend.user_service.infraestructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.renewsim.backend.role_service.dto.RoleDTO;

@FeignClient(name = "role-service", url = "${role-service.url}", configuration = FeignRoleConfig.class)
public interface RoleServiceClient {

    @GetMapping("/api/v1/roles/exists/{name}")
    boolean existsRole(@PathVariable("name") String name);

      @GetMapping("/api/v1/roles/by-name/{name}")
    RoleDTO findByName(@PathVariable("name") String name);

}
