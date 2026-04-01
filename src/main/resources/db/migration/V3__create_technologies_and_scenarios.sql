-- =====================================================
-- V3: Create technologies and scenarios tables
-- Requirements: 6.1, 8.1
-- =====================================================

-- =====================================================
-- TECHNOLOGIES TABLE
-- =====================================================
CREATE TABLE technologies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Technology name (e.g., Solar PV, Wind Turbine)',
    energy_type ENUM('SOLAR', 'WIND', 'HYDRO', 'GEOTHERMAL', 'BIOMASS') NOT NULL COMMENT 'Type of renewable energy',
    description TEXT COMMENT 'Technology description',
    
    -- Financial parameters
    unit_cost DECIMAL(15,2) NOT NULL COMMENT 'Cost per unit (currency/kW)',
    maintenance_cost DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Annual maintenance cost percentage',
    lifespan_years INT NOT NULL COMMENT 'Expected lifespan in years',
    
    -- Technical parameters
    efficiency DECIMAL(5,2) NOT NULL COMMENT 'Energy conversion efficiency (0-100%)',
    capacity_factor DECIMAL(5,2) NOT NULL COMMENT 'Capacity factor (0-100%)',
    min_capacity_kw DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Minimum capacity in kW',
    max_capacity_kw DECIMAL(10,2) COMMENT 'Maximum capacity in kW (NULL = no limit)',
    
    -- Environmental parameters
    co2_reduction_factor DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT 'CO2 reduction per kWh',
    
    -- Status and metadata
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Technology available for simulations',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    CONSTRAINT chk_technologies_efficiency CHECK (efficiency >= 0 AND efficiency <= 100),
    CONSTRAINT chk_technologies_capacity_factor CHECK (capacity_factor >= 0 AND capacity_factor <= 100),
    CONSTRAINT chk_technologies_lifespan CHECK (lifespan_years > 0),
    CONSTRAINT chk_technologies_unit_cost CHECK (unit_cost >= 0),
    CONSTRAINT chk_technologies_maintenance_cost CHECK (maintenance_cost >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Renewable energy technologies';

-- Indexes for technology queries (Requirement 6.1)
CREATE INDEX idx_technologies_energy_type ON technologies(energy_type);
CREATE INDEX idx_technologies_is_active ON technologies(is_active);
CREATE INDEX idx_technologies_name ON technologies(name);

-- =====================================================
-- SCENARIOS TABLE
-- =====================================================
CREATE TABLE scenarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT 'Scenario name',
    description TEXT COMMENT 'Scenario description',
    technology_id BIGINT NOT NULL COMMENT 'Associated technology',
    
    -- Climate and location data
    climate_profile JSON COMMENT 'Climate data (temperature, irradiance, wind speed, etc.)',
    location_lat DECIMAL(10,8) COMMENT 'Latitude',
    location_lng DECIMAL(11,8) COMMENT 'Longitude',
    
    -- Simulation parameters
    capacity_kw DECIMAL(10,2) NOT NULL COMMENT 'Installed capacity in kW',
    simulation_years INT NOT NULL DEFAULT 25 COMMENT 'Simulation duration in years',
    
    -- Results (nullable until calculated)
    total_energy_generated_kwh DECIMAL(15,2) COMMENT 'Total energy generated',
    total_cost DECIMAL(15,2) COMMENT 'Total project cost',
    roi_percentage DECIMAL(5,2) COMMENT 'Return on investment',
    co2_avoided_kg DECIMAL(15,2) COMMENT 'Total CO2 emissions avoided',
    
    -- Metadata
    created_by BIGINT COMMENT 'User who created the scenario',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    CONSTRAINT fk_scenarios_technology FOREIGN KEY (technology_id) REFERENCES technologies(id) ON DELETE CASCADE,
    CONSTRAINT fk_scenarios_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_scenarios_capacity CHECK (capacity_kw > 0),
    CONSTRAINT chk_scenarios_simulation_years CHECK (simulation_years > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Simulation scenarios';

-- Indexes for scenario queries (Requirement 8.1)
CREATE INDEX idx_scenarios_technology_id ON scenarios(technology_id);
CREATE INDEX idx_scenarios_created_by ON scenarios(created_by);
CREATE INDEX idx_scenarios_created_at ON scenarios(created_at);