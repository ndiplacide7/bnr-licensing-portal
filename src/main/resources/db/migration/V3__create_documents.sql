CREATE TABLE bnr.documents (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id      UUID         NOT NULL REFERENCES bnr.applications(id),
    uploader_id         UUID         NOT NULL REFERENCES bnr.users(id),
    document_type       VARCHAR(100) NOT NULL,
    original_name       VARCHAR(255) NOT NULL,
    storage_path        VARCHAR(500) NOT NULL,
    mime_type           VARCHAR(100) NOT NULL,
    size_bytes          BIGINT       NOT NULL,
    checksum_sha256     VARCHAR(64)  NOT NULL,
    iteration           INTEGER      NOT NULL DEFAULT 1,
    is_current_version  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

COMMENT ON COLUMN bnr.documents.checksum_sha256 IS
    'SHA-256 hex digest. Used to prove file integrity if records are presented as legal evidence.';
COMMENT ON COLUMN bnr.documents.size_bytes IS
    'BIGINT — semantically correct for byte counts even though INTEGER handles small files.';
COMMENT ON COLUMN bnr.documents.iteration IS
    'Submission round number. Increments each time the applicant resubmits documents.';
COMMENT ON COLUMN bnr.documents.is_current_version IS
    'Service sets existing rows to FALSE before inserting new documents on resubmission.';
