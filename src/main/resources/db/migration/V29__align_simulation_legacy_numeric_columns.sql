-- =====================================================
-- V29: Align remaining legacy simulations numeric columns with JPA Double mapping
-- =====================================================

ALTER TABLE simulations
    MODIFY COLUMN location_lat DOUBLE NOT NULL COMMENT 'Latitude';

ALTER TABLE simulations
    MODIFY COLUMN location_lng DOUBLE NOT NULL COMMENT 'Longitude';

ALTER TABLE simulations
    MODIFY COLUMN capacity_kw DOUBLE NOT NULL COMMENT 'Installed capacity in kW';

ALTER TABLE simulations
    MODIFY COLUMN energy_generated DOUBLE NOT NULL DEFAULT 0 COMMENT 'Total energy generated (kWh)';
