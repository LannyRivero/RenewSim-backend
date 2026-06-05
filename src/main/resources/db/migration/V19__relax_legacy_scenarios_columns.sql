-- =====================================================
-- V19: Relax legacy scenario columns still blocking new inserts
-- Requirements: 8.1, 8.2, 8.3
-- =====================================================

ALTER TABLE scenarios
    MODIFY COLUMN capacity_kw DECIMAL(10,2) NULL;
