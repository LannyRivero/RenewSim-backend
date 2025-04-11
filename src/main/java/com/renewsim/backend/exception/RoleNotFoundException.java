package com.renewsim.backend.exception;

public class RoleNotFoundException extends ResourceNotFoundException {
    public RoleNotFoundException(String roleName) {
        super("Role not found with name: " + roleName);
    }
}


