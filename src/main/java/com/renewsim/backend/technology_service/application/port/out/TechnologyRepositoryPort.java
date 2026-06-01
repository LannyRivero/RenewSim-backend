package com.renewsim.backend.technology_service.application.port.out;

import java.util.List;
import java.util.Optional;

import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.EnergyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TechnologyRepositoryPort {

    Technology save(Technology technology);

    Optional<Technology> findById(Long id);

    Optional<Technology> findByName(String name);

    List<Technology> findAll();

    Page<Technology> findAll(Pageable pageable);

    Page<Technology> findByEnergyType(EnergyType energyType, Pageable pageable);

    Page<Technology> findAllActive(Pageable pageable);

    void deleteById(Long id);

    boolean existsByName(String name);
}



 

   

  


    
   
