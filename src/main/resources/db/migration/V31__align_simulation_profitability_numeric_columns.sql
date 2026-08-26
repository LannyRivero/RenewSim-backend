-- =====================================================
-- V31: Align remaining simulation profitability columns with JPA Double mapping
-- =====================================================

ALTER TABLE simulations
    MODIFY COLUMN npv DOUBLE NULL COMMENT 'Net Present Value';

ALTER TABLE simulations
    MODIFY COLUMN irr_pct DOUBLE NULL COMMENT 'Internal Rate of Return (%)';
