CREATE SCHEMA IF NOT EXISTS bnr;

CREATE TABLE bnr.users (
    id                      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email                   VARCHAR(255) NOT NULL UNIQUE,
    password_hash           VARCHAR(255) NOT NULL,
    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(100) NOT NULL,
    role                    VARCHAR(50)  NOT NULL,
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_login_attempts   INTEGER      NOT NULL DEFAULT 0,
    locked_until            TIMESTAMPTZ,
    last_login_at           TIMESTAMPTZ,
    password_changed_at     TIMESTAMPTZ,
    created_by              UUID REFERENCES bnr.users(id),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now()
);

COMMENT ON COLUMN bnr.users.role IS
    'Stored without ROLE_ prefix. Spring Security hasRole() prepends it automatically.';
COMMENT ON COLUMN bnr.users.is_active IS
    'Users are never deleted — deactivated only. Preserves audit integrity and referential consistency.';
COMMENT ON COLUMN bnr.users.locked_until IS
    'NULL = not locked. Set after N consecutive failed_login_attempts.';
