-- Performance indexes for common query patterns

CREATE INDEX idx_applications_applicant   ON bnr.applications(applicant_id);
CREATE INDEX idx_applications_reviewer    ON bnr.applications(reviewer_id);
CREATE INDEX idx_applications_status      ON bnr.applications(status);
CREATE INDEX idx_documents_application    ON bnr.documents(application_id);
CREATE INDEX idx_documents_current        ON bnr.documents(application_id, is_current_version) WHERE is_current_version = TRUE;
CREATE INDEX idx_audit_logs_application   ON bnr.audit_logs(application_id);
CREATE INDEX idx_audit_logs_actor         ON bnr.audit_logs(actor_id);
CREATE INDEX idx_audit_logs_created_at    ON bnr.audit_logs(created_at DESC);
CREATE INDEX idx_users_email              ON bnr.users(email);
