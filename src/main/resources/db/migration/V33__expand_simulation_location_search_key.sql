DROP INDEX idx_simulations_created_by_location_search_key ON simulations;

ALTER TABLE simulations
    MODIFY COLUMN location_search_key TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL;

CREATE INDEX idx_simulations_history_created_by ON simulations (created_by);
