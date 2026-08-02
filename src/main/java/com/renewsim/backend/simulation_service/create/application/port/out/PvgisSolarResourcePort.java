package com.renewsim.backend.simulation_service.create.application.port.out;

import java.util.List;

public interface PvgisSolarResourcePort {

    PvgisSolarResourceProfile fetchProfile(double latitude, double longitude, double systemLossPct);

    record PvgisSolarResourceProfile(
            List<Double> monthlyGenerationPerKwp,
            List<Double> monthlyIrradianceKwhM2,
            List<Double> monthlyTemperatureC,
            String climatePeriod,
            String source) {
    }
}
