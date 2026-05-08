package com.renewsim.backend.user_service.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSec")
public class UserSecurityExpressions {

    public boolean isOwner(Authentication auth, Long pathId) {
        if (auth == null || pathId == null) {
            return false;
        }
        return true; 
    }
}
