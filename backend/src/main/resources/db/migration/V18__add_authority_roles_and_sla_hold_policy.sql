INSERT INTO identity.role (role_id, code, name)
VALUES
    (gen_random_uuid(), 'AUTHORITY_ADMIN', 'Authority Administrator'),
    (gen_random_uuid(), 'AUTHORITY_OFFICER', 'Authority Officer'),
    (gen_random_uuid(), 'SYSTEM_ADMIN', 'System Administrator')
ON CONFLICT (code) DO NOTHING;

ALTER TABLE sla.sla_policy
    ADD COLUMN hold_pauses_sla BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX uq_sla_policy_active_match
    ON sla.sla_policy (category_id, COALESCE(subcategory_id, '00000000-0000-0000-0000-000000000000'::UUID), severity)
    WHERE is_active = TRUE;
