-- =====================================================
-- V30: Align remaining simulation financial numeric columns with JPA Double mapping
-- =====================================================

ALTER TABLE simulations
    MODIFY COLUMN initial_investment DOUBLE NOT NULL COMMENT 'Initial investment cost';

ALTER TABLE simulations
    MODIFY COLUMN total_cost DOUBLE NOT NULL COMMENT 'Total project cost (NPV)';
