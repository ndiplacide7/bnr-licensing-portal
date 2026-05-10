CREATE TABLE bnr.applications (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    applicant_id        UUID         NOT NULL REFERENCES bnr.users(id),
    reviewer_id         UUID                  REFERENCES bnr.users(id),
    approver_id         UUID                  REFERENCES bnr.users(id),
    institution_name    VARCHAR(255) NOT NULL,
    license_type        VARCHAR(100) NOT NULL,
    status              VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    decision_notes      TEXT,
    withdrawal_reason   TEXT,
    submitted_at        TIMESTAMPTZ,
    reviewed_at         TIMESTAMPTZ,
    decided_at          TIMESTAMPTZ,
    version             INTEGER      NOT NULL DEFAULT 1,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

COMMENT ON COLUMN bnr.applications.version IS
    'Optimistic locking counter — prevents concurrent modification race conditions.';
COMMENT ON COLUMN bnr.applications.decision_notes IS
    'Enforced NOT NULL at service layer when status = APPROVED or REJECTED.';
COMMENT ON COLUMN bnr.applications.withdrawal_reason IS
    'Enforced NOT NULL at service layer when status = WITHDRAWN.';
COMMENT ON COLUMN bnr.applications.submitted_at IS
    'Timestamp when DRAFT transitioned to SUBMITTED — distinct from created_at (draft start).';
