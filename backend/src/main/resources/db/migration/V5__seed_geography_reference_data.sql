-- ============================================================
-- Civic Transparency
-- Flyway Migration V5
-- Initial Geography Reference Data
--
-- Initial supported geography:
--   Karnataka -> Bengaluru
--
-- This is development/reference data for the initial rollout.
-- V2 remains unchanged.
-- ============================================================

INSERT INTO geography.state (
    state_id,
    code,
    name,
    is_active
)
VALUES (
    '11111111-1111-4111-8111-111111111111',
    'KA',
    'Karnataka',
    TRUE
);

INSERT INTO geography.city (
    city_id,
    state_id,
    code,
    name,
    is_active
)
VALUES (
    '22222222-2222-4222-8222-222222222222',
    '11111111-1111-4111-8111-111111111111',
    'BLR',
    'Bengaluru',
    TRUE
);
