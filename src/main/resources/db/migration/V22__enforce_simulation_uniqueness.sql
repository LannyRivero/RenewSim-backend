-- =====================================================
-- V22: Enforce simulation uniqueness for owner + identity fields
-- Prevents concurrent duplicate inserts that app-level checks cannot stop.
-- =====================================================

UPDATE simulations
SET name = COALESCE(NULLIF(TRIM(name), ''), CONCAT('Simulation ', id));

UPDATE simulations
SET energy_type = COALESCE(NULLIF(TRIM(energy_type), ''), 'SOLAR');

UPDATE simulations
SET created_by = COALESCE(NULLIF(TRIM(created_by), ''), 'system');

UPDATE simulations
SET location_lat = COALESCE(location_lat, 0),
    location_lng = COALESCE(location_lng, 0);

UPDATE simulations s
JOIN (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY created_by, name, energy_type, location_lat, location_lng
               ORDER BY id
           ) AS duplicate_rank
    FROM simulations
) ranked ON ranked.id = s.id
SET s.name = CONCAT(s.name, ' #', s.id)
WHERE ranked.duplicate_rank > 1;

ALTER TABLE simulations
    MODIFY COLUMN name VARCHAR(255) NOT NULL,
    MODIFY COLUMN energy_type VARCHAR(32) NOT NULL,
    MODIFY COLUMN location_lat DECIMAL(10,8) NOT NULL,
    MODIFY COLUMN location_lng DECIMAL(11,8) NOT NULL,
    MODIFY COLUMN created_by VARCHAR(100) NOT NULL;

ALTER TABLE simulations
    ADD CONSTRAINT uk_simulations_owner_name_energy_location
        UNIQUE (created_by, name, energy_type, location_lat, location_lng);
