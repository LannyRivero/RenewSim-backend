-- =====================================================
-- V28: Align simulations annual energy column with JPA Double mapping
-- =====================================================

ALTER TABLE simulations
    MODIFY COLUMN annual_energy_generated DOUBLE NULL COMMENT 'Average annual energy (kWh/year)';
