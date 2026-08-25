-- =====================================================
-- V15: Align simulations persistence contract with backend baseline
-- Scope: conservative hardening for simulation foundation
-- =====================================================

-- 1) Keep legacy V4 columns, but relax strict NOT NULL constraints that
--    are no longer written by current simulation persistence flow.
ALTER TABLE simulations
    MODIFY COLUMN user_id BIGINT NULL,
    MODIFY COLUMN technology_id BIGINT NULL,
    MODIFY COLUMN location_lat DECIMAL(10,8) NULL,
    MODIFY COLUMN location_lng DECIMAL(11,8) NULL,
    MODIFY COLUMN capacity_kw DECIMAL(10,2) NULL,
    MODIFY COLUMN initial_investment DECIMAL(15,2) NULL,
    MODIFY COLUMN total_cost DECIMAL(15,2) NULL;

-- 2) Add the baseline columns expected by SimulationEntity/SimulationMapper.
ALTER TABLE simulations
    ADD COLUMN location VARCHAR(255) NULL AFTER technology_id;

ALTER TABLE simulations
    ADD COLUMN energy_type VARCHAR(32) NULL AFTER location;

ALTER TABLE simulations
    ADD COLUMN project_size DOUBLE NULL AFTER energy_type;

ALTER TABLE simulations
    ADD COLUMN budget DOUBLE NULL AFTER project_size;

ALTER TABLE simulations
    ADD COLUMN estimated_energy DOUBLE NOT NULL DEFAULT 0 AFTER budget;

ALTER TABLE simulations
    ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'system' AFTER co2_reduction;

-- 3) Add many-to-many storage used by SimulationEntity.technologyIds.
CREATE TABLE simulation_technologies (
    simulation_id BIGINT NOT NULL,
    technology_id BIGINT NOT NULL,
    PRIMARY KEY (simulation_id, technology_id),
    CONSTRAINT fk_simulation_technologies_simulation
        FOREIGN KEY (simulation_id) REFERENCES simulations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_simulation_technologies_technology
    ON simulation_technologies(technology_id);
