INSERT INTO identity.role (role_id, code, name)
VALUES (
    gen_random_uuid(),
    'CITIZEN',
    'Citizen'
)
ON CONFLICT (code) DO NOTHING;
