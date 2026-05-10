package rw.gov.bnr.bnrlicensingportal.domain.enums;

public enum AuditAction {
    // Application lifecycle
    APPLICATION_CREATED,
    APPLICATION_SUBMITTED,
    APPLICATION_ASSIGNED,
    APPLICATION_UNDER_REVIEW,
    REVIEW_COMPLETED,
    APPLICATION_APPROVED,
    APPLICATION_REJECTED,
    APPLICATION_WITHDRAWN,
    // Documents
    DOCUMENT_UPLOADED,
    // User management
    USER_CREATED,
    USER_DEACTIVATED,
    // Authentication
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    PASSWORD_CHANGED,
    ACCOUNT_LOCKED
}
