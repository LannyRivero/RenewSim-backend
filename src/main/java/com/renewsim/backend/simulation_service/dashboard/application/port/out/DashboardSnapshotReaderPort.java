package com.renewsim.backend.simulation_service.dashboard.application.port.out;

import com.renewsim.backend.simulation_service.dashboard.application.projection.DashboardSnapshotData;

public interface DashboardSnapshotReaderPort {

    DashboardSnapshotData read(String raw);
}
