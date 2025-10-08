package com.renewsim.backend.technology_service.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.domain.exception.TechnologyNotFoundException;
import com.renewsim.backend.technology_service.domain.model.Technology;

@Component
@RequiredArgsConstructor
public class TechnologyValidator {

    private final TechnologyRepositoryPort repository;

    public void ensureUniqueName(String name) {
        if (repository.existsByName(name)) {
            throw new IllegalArgumentException("Technology with name '" + name + "' already exists");
        }
    }

    public void ensureExists(Long id) {
        if (repository.findById(id).isEmpty()) {
            throw new TechnologyNotFoundException(id);
        }
    }

    public Technology getExisting(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TechnologyNotFoundException(id));
    }
}

