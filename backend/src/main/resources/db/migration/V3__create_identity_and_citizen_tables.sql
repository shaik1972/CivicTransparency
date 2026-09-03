-- ============================================================
-- Civic Transparency
-- Flyway Migration V3
-- Identity and Citizen foundation
--
-- Design authority:
-- Detailed Design Part 2A / Part 2B
-- ============================================================

CREATE TABLE identity.user_account (
    user_id UUID PRIMARY KEY,
    username VARCHAR(100) UNIQUE,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE identity.role (
    role_id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE identity.user_role (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    CONSTRAINT pk_user_role PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id)
        REFERENCES identity.user_account (user_id),
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id)
        REFERENCES identity.role (role_id)
);

CREATE TABLE citizen.citizen_profile (
    citizen_id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    public_alias VARCHAR(100),
    profile_status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_citizen_profile_user
        FOREIGN KEY (user_id)
        REFERENCES identity.user_account (user_id)
);

CREATE TABLE citizen.citizen_contact (
    citizen_id UUID PRIMARY KEY,
    phone_encrypted TEXT NOT NULL,
    email_encrypted TEXT NOT NULL,
    verified_phone BOOLEAN NOT NULL,
    verified_email BOOLEAN NOT NULL,

    CONSTRAINT fk_citizen_contact_profile
        FOREIGN KEY (citizen_id)
        REFERENCES citizen.citizen_profile (citizen_id)
);

CREATE TABLE citizen.citizen_address (
    address_id UUID PRIMARY KEY,
    citizen_id UUID NOT NULL,
    address_text_encrypted TEXT NOT NULL,
    city_id UUID,
    postal_code VARCHAR(20),
    is_primary BOOLEAN NOT NULL,

    CONSTRAINT fk_citizen_address_profile
        FOREIGN KEY (citizen_id)
        REFERENCES citizen.citizen_profile (citizen_id),
    CONSTRAINT fk_citizen_address_city
        FOREIGN KEY (city_id)
        REFERENCES geography.city (city_id)
);
