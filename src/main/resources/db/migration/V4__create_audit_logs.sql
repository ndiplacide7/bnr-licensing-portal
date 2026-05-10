CREATE TABLE bnr.audit_logs (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id      UUID                  REFERENCES bnr.applications(id),
    actor_id            UUID         NOT NULL REFERENCES bnr.users(id),
    action              VARCHAR(100) NOT NULL,
    previous_state      JSONB,
    new_state           JSONB,
    ip_address          VARCHAR(45)  NOT NULL,
    user_agent          TEXT,
    session_id          VARCHAR(100),
    correlation_id      UUID,
    severity            VARCHAR(20)  NOT NULL DEFAULT 'INFO',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
    -- NO updated_at. Intentional. Audit records are append-only by design.
);

-- Append-only enforcement at the database level (run as superuser in production):
--   REVOKE UPDATE, DELETE ON bnr.audit_logs FROM bnr_app_user;
-- The application DB user may only INSERT and SELECT on this table.

COMMENT ON TABLE  bnr.audit_logs IS
    'Append-only. Records may be used as legal evidence — no row is ever modified or deleted.';
COMMENT ON COLUMN bnr.audit_logs.application_id IS
    'Nullable: some actions (login, user creation) are not tied to a specific application.';
COMMENT ON COLUMN bnr.audit_logs.ip_address IS
    'VARCHAR(45) covers IPv4 (max 15 chars) and IPv6 (max 39 chars).';
COMMENT ON COLUMN bnr.audit_logs.severity IS
    'INFO | WARNING | CRITICAL — enables compliance officers to filter high-priority events.';
