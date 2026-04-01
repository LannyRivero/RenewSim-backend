-- =====================================================
-- V4: Create simulations tables
-- Requirement: 15.1
-- =====================================================

-- =====================================================
-- SIMULATIONS TABLE
-- =====================================================
CREATE TABLE simulations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'User who owns the simulation',
    technology_id BIGINT NOT NULL COMMENT 'Technology used',
    
    -- Location data
    location_name VARCHAR(255) COMMENT 'Human-readable location name',
    location_lat DECIMAL(10,8) NOT NULL COMMENT 'Latitude',
    location_lng DECIMAL(11,8) NOT NULL COMMENT 'Longitude',
    
    -- Climate data (stored as JSON for flexibility)
    climate_data JSON COMMENT 'Climate parameters (temperature, irradiance, wind, etc.)',
    
    -- Simulation parameters
    capacity_kw DECIMAL(10,2) NOT NULL COMMENT 'Installed capacity in kW',
    simulation_years INT NOT NULL DEFAULT 25 COMMENT 'Simulation duration in years',
    discount_rate DECIMAL(5,2) DEFAULT 5.00 COMMENT 'Financial discount rate (%)',
    
    -- Financial results
    initial_investment DECIMAL(15,2) NOT NULL COMMENT 'Initial investment cost',
    annual_maintenance_cost DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Annual maintenance cost',
    total_cost DECIMAL(15,2) NOT NULL COMMENT 'Total project cost (NPV)',
    
    -- Energy results
    energy_generated DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT 'Total energy generated (kWh)',
    annual_energy_generated DECIMAL(15,2) COMMENT 'Average annual energy (kWh/year)',
    
    -- Environmental results
    co2_reduction DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT 'Total CO2 avoided (kg)',
    
    -- Economic results
    roi_percentage DECIMAL(5,2) COMMENT 'Return on investment (%)',
    payback_period_years DECIMAL(5,2) COMMENT 'Payback period (years)',
    npv DECIMAL(15,2) COMMENT 'Net Present Value',
    
    -- Status and metadata
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    error_message TEXT COMMENT 'Error details if status=FAILED',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    completed_at TIMESTAMP(6) NULL COMMENT 'Timestamp when simulation completed',
    
    CONSTRAINT fk_simulations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_simulations_technology FOREIGN KEY (technology_id) REFERENCES technologies(id) ON DELETE RESTRICT,
    CONSTRAINT chk_simulations_capacity CHECK (capacity_kw > 0),
    CONSTRAINT chk_simulations_years CHECK (simulation_years > 0),
    CONSTRAINT chk_simulations_discount_rate CHECK (discount_rate >= 0 AND discount_rate <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Energy simulations';

-- Compound indexes for simulation queries (Requirement 15.1)
CREATE INDEX idx_simulations_user_status ON simulations(user_id, status);
CREATE INDEX idx_simulations_technology ON simulations(technology_id);
CREATE INDEX idx_simulations_created_at ON simulations(created_at);
CREATE INDEX idx_simulations_status ON simulations(status);

-- =====================================================
-- SIMULATION_SHARE_TOKENS TABLE
-- =====================================================
CREATE TABLE simulation_share_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    simulation_id BIGINT NOT NULL COMMENT 'Simulation being shared',
    token VARCHAR(64) NOT NULL UNIQUE COMMENT 'Unique share token',
    created_by BIGINT NOT NULL COMMENT 'User who created the share link',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at TIMESTAMP(6) NOT NULL COMMENT 'Share link expiration',
    access_count INT NOT NULL DEFAULT 0 COMMENT 'Number of times accessed',
    last_accessed_at TIMESTAMP(6) NULL COMMENT 'Last access timestamp',
    
    CONSTRAINT fk_share_tokens_simulation FOREIGN KEY (simulation_id) REFERENCES simulations(id) ON DELETE CASCADE,
    CONSTRAINT fk_share_tokens_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Simulation share links';

-- Indexes for share token lookups
CREATE INDEX idx_share_tokens_token ON simulation_share_tokens(token);
CREATE INDEX idx_share_tokens_simulation ON simulation_share_tokens(simulation_id);
CREATE INDEX idx_share_tokens_expires_at ON simulation_share_tokens(expires_at);