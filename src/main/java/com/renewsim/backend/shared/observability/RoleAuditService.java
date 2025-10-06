package com.renewsim.backend.shared.observability;

import org.springframework.stereotype.Service;

@Service
public class RoleAuditService {

    public void roleAssigned(Long requesterId, Long targetUserId, String roleName) {
        RoleAuditLogger.roleAssigned(requesterId, targetUserId, roleName);
    }

    public void roleRevoked(Long requesterId, Long targetUserId, String roleName) {
        RoleAuditLogger.roleRevoked(requesterId, targetUserId, roleName);
    }
}

