package com.renewsim.backend.auth_service.infrastructure.persistence.adapter;

import com.renewsim.backend.auth_service.application.port.out.ActivationTokenRepositoryPort;
import com.renewsim.backend.auth_service.domain.model.ActivationToken;
import com.renewsim.backend.auth_service.infrastructure.persistence.entity.ActivationTokenEntity;
import com.renewsim.backend.auth_service.infrastructure.persistence.repo.ActivationTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ActivationTokenPersistenceAdapter implements ActivationTokenRepositoryPort {

    private final ActivationTokenJpaRepository repo;

    @Override
    @Transactional
    public ActivationToken save(ActivationToken token) {
        ActivationTokenEntity entity = toEntity(token);
        ActivationTokenEntity saved = repo.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ActivationToken> findByTokenHash(String tokenHash) {
        return repo.findByTokenHash(tokenHash).map(this::toDomain);
    }

    // --- Mapping ---

    private ActivationTokenEntity toEntity(ActivationToken domain) {
        ActivationTokenEntity entity = new ActivationTokenEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setTokenHash(domain.getTokenHash());
        entity.setIssuedAt(domain.getIssuedAt().toInstant(ZoneOffset.UTC));
        entity.setExpiresAt(domain.getExpiresAt().toInstant(ZoneOffset.UTC));
        if (domain.isUsed()) {
            entity.setActivatedAt(domain.getIssuedAt().toInstant(ZoneOffset.UTC));
        }
        return entity;
    }

    private ActivationToken toDomain(ActivationTokenEntity entity) {
        return ActivationToken.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                LocalDateTime.ofInstant(entity.getIssuedAt(), ZoneOffset.UTC),
                LocalDateTime.ofInstant(entity.getExpiresAt(), ZoneOffset.UTC),
                entity.isUsed());
    }
}