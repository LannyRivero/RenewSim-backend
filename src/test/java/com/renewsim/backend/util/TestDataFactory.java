package com.renewsim.backend.util;

import com.renewsim.backend.role.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.user.User;

import java.util.Set;

public class TestDataFactory {

    public static Role createRole(RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }

    public static User createUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("securepassword"); 
        user.setRoles(Set.of(role));
        return user;
    }

    public static User createUser(String username, String password, Set<Role> roles) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRoles(roles);
        return user;
    }

    public static User createUserWithoutRoles(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRoles(Set.of());
        return user;
    }
}

