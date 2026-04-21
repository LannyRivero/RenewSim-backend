package com.renewsim.backend.auth_service.infrastructure.persistence.adapter;

import com.renewsim.backend.auth_service.application.port.out.TokenBlacklistPort;
import com.renewsim.backend.auth_service.infrastructure.persistence.entity.TokenBlacklistEntity;
import com.renewsim.backend.auth_service.infrastructure.persistence.repo.TokenBlacklistJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TokenBlacklistAdapter implements TokenBlacklistPort {

    private final TokenBlacklistJpaRepository repo;

    @Override
    @Transactional
    public void blacklist(String jti, Long userId, long expiresAt) {
        TokenBlacklistEntity entity = new TokenBlacklistEntity();
        entity.setJti(jti);
        entity.setUserId(userId);
        entity.setExpiresAt(Instant.ofEpochSecond(expiresAt));
        entity.setReason("logout");
        repo.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlacklisted(String jti) {
        return repo.existsByJti(jti);
    }
}