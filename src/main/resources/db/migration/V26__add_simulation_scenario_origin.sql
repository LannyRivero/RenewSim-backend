-- =====================================================
-- V26: Add optional scenario origin reference to simulations
-- Requirements: phase 7 (simulation from scenario origin)
-- =====================================================

SET @ddl = IF (
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'simulations'
          AND column_name = 'scenario_id'
    ),
    'SELECT 1',
    'ALTER TABLE simulations ADD COLUMN scenario_id BIGINT NULL AFTER recommendation'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_simulations_scenario_id
    ON simulations(scenario_id);
