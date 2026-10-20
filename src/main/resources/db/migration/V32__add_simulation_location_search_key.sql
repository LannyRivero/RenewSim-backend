ALTER TABLE simulations
    ADD COLUMN location_search_key VARCHAR(255) NULL;

UPDATE simulations
SET location_search_key = LOWER(REGEXP_REPLACE(TRIM(location), '[[:space:]]+', ' '))
WHERE location IS NOT NULL;

CREATE INDEX idx_simulations_created_by_location_search_key
    ON simulations (created_by, location_search_key);
