package com.renewsim.backend.role;

import java.util.List;
import java.util.Set;

import com.renewsim.backend.role_service.domain.model.RoleName;

public interface RoleService {

    Role getRoleByName(RoleName roleName);

    Set<Role> getRolesFromStrings(Set<String> roleNames);

    Set<Role> getRolesByNames(List<String> roleNames);
}


