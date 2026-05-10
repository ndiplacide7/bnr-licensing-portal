package rw.gov.bnr.bnrlicensingportal.api.dto.response;

import rw.gov.bnr.bnrlicensingportal.domain.enums.AuditAction;
import rw.gov.bnr.bnrlicensingportal.domain.enums.AuditSeverity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID applicationId,
        UserResponse actor,
        AuditAction action,
        String previousState,
        String newState,
        String ipAddress,
        AuditSeverity severity,
        OffsetDateTime createdAt
) {}
