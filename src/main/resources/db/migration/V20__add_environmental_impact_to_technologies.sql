-- =====================================================
-- V20: Add environmental impact persistence for technologies
-- Requirements: 5.1, 5.6
-- =====================================================

ALTER TABLE technologies
    ADD COLUMN environmental_impact DECIMAL(5,2) NULL AFTER co2_reduction_factor;

UPDATE technologies
SET environmental_impact = CASE
    WHEN name = 'Panel Solar Fotovoltaico' THEN 15.00
    WHEN name = 'Turbina Eolica Terrestre' THEN 10.00
    WHEN name = 'Microcentral Hidroelectrica' THEN 20.00
    ELSE 0.00
END
WHERE environmental_impact IS NULL;

ALTER TABLE technologies
    MODIFY COLUMN environmental_impact DECIMAL(5,2) NOT NULL;
