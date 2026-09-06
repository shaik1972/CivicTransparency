CREATE TABLE authority.authority (
    authority_id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE,
    name VARCHAR(200) NOT NULL,
    state_id UUID REFERENCES geography.state(state_id),
    city_id UUID REFERENCES geography.city(city_id),
    status VARCHAR(30) NOT NULL
);

CREATE TABLE authority.authority_department (
    department_id UUID PRIMARY KEY,
    authority_id UUID REFERENCES authority.authority(authority_id),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(30) NOT NULL
);

CREATE TABLE authority.authority_user (
    authority_user_id UUID PRIMARY KEY,
    user_id UUID UNIQUE REFERENCES identity.user_account(user_id),
    department_id UUID REFERENCES authority.authority_department(department_id),
    employee_reference VARCHAR(100)
);
