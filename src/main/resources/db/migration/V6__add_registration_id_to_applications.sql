ALTER TABLE bnr.applications
    ADD COLUMN registration_id VARCHAR(9) NOT NULL UNIQUE;

COMMENT ON COLUMN bnr.applications.registration_id IS
    'Unique 9-digit identifier of the applying institution. Supplied by the applicant at creation time.';
