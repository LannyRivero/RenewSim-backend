package com.renewsim.backend.simulation_service.application.port.out;

public interface TechnologyLookupPort {

    boolean existsActiveByEnergyType(String energyType);
}
