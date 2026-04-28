package com.renewsim.backend.auth_service.infrastructure.persistence.adapter;

import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.auth_service.domain.model.RefreshToken;
import com.renewsim.backend.auth_service.infrastructure.persistence.entity.RefreshTokenEntity;
import com.renewsim.backend.auth_service.infrastructure.persistence.repo.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository repo;

    @Override
    @Transactional
    public RefreshToken save(RefreshToken token) {
        RefreshTokenEntity entity = toEntity(token);
        RefreshTokenEntity saved = repo.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return repo.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(Long userId) {
        repo.revokeAllByUserId(userId, Instant.now());
    }

    private RefreshTokenEntity toEntity(RefreshToken domain) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setTokenHash(domain.getTokenHash());
        entity.setIssuedAt(domain.getIssuedAt().toInstant(ZoneOffset.UTC));
        entity.setExpiresAt(domain.getExpiresAt().toInstant(ZoneOffset.UTC));
        entity.setRevoked(domain.isRevoked());
        if (domain.getRevokedAt() != null) {
            entity.setRevokedAt(domain.getRevokedAt().toInstant(ZoneOffset.UTC));
        }
        return entity;
    }

    private RefreshToken toDomain(RefreshTokenEntity entity) {
        return RefreshToken.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                LocalDateTime.ofInstant(entity.getIssuedAt(), ZoneOffset.UTC),
                LocalDateTime.ofInstant(entity.getExpiresAt(), ZoneOffset.UTC),
                entity.isRevoked(),
                entity.getRevokedAt() != null ? LocalDateTime.ofInstant(entity.getRevokedAt(), ZoneOffset.UTC) : null);
    }
}