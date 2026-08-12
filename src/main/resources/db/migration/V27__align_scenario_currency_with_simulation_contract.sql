-- =====================================================
-- V27: Align scenario currency with simulation contract
-- simulation_service currently supports EUR-only economics snapshots
-- =====================================================

UPDATE scenarios
SET default_investment_currency = 'EUR'
WHERE default_investment_currency = 'USD';
