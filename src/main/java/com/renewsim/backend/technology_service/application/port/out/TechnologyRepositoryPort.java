package com.renewsim.backend.technology_service.application.port.out;

import java.util.List;
import java.util.Optional;

import com.renewsim.backend.technology_service.domain.model.Technology;

public interface TechnologyRepositoryPort {

    Technology save(Technology technology);

    Optional<Technology> findById(Long id);

    Optional<Technology> findByName(String name);

    List<Technology> findAll();

    void deleteById(Long id);

    boolean existsByName(String name);
}



 

   

  


    
   