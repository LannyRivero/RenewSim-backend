package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.renewsim.backend.simulation_service.application.port.out.SimulationRepositoryPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.infrastructure.mapper.SimulationMapper;
import com.renewsim.backend.shared.exception.ConflictException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SimulationRepositoryAdapter implements SimulationRepositoryPort {

    private static final String SIMULATION_DUPLICATE_CONSTRAINT = "uk_simulations_owner_name_energy_location";

    private final JpaSimulationRepository repository;
    private final SimulationMapper mapper;

    @Override
    public Simulation save(Simulation simulation) {
        try {
            return mapper.toDomain(
                    repository.save(mapper.toEntity(simulation)));
        } catch (DataIntegrityViolationException ex) {
            String message = ex.getMostSpecificCause() != null
                    ? ex.getMostSpecificCause().getMessage()
                    : "";
            log.warn("Integrity violation while saving simulation name={} owner={}: {}",
                    simulation.name(), simulation.createdBy(), message);

            if (message.contains(SIMULATION_DUPLICATE_CONSTRAINT)) {
                throw new ConflictException("Simulation already exists for same name, coordinates, and technology", ex);
            }
            throw ex;
        }
    }

    @Override
    public List<Simulation> findAllByCreatedBy(String username) {
        return repository.findByCreatedByOrderByCreatedAtDesc(username)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Simulation> findDuplicate(String username, String name, String energyType, double latitude, double longitude) {
        return repository.findFirstByCreatedByAndNameAndEnergyTypeAndLocationLatAndLocationLng(
                        username,
                        name,
                        energyType,
                        latitude,
                        longitude)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Simulation> findById(Long id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAllByCreatedBy(String username) {
        repository.deleteAllByCreatedBy(username);
    }

}
