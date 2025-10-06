package com.renewsim.backend.technology_service.infrastructure.persistence.repository;

import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTechnologyRepository extends JpaRepository<TechnologyEntity, Long> {

    boolean existsByName(String name);
    
    Optional<TechnologyEntity> findByName(String name);
}
